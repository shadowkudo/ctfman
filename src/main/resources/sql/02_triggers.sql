SET search_path = ctfman;
BEGIN;

-- NOTE: Whenever a table has a deleted_at field, it should be taken into account
-- as the rules don't always apply the same way for soft deleted elements

-- NOTE: Helpers

DROP PROCEDURE IF EXISTS create_authentication CASCADE;
CREATE PROCEDURE create_authentication(identification VARCHAR(64), password_hash VARCHAR(64))
LANGUAGE plpgsql AS $$
BEGIN
  INSERT INTO authentication (identification, password_hash)
    VALUES (identification, password_hash);
END;
$$;

DROP PROCEDURE IF EXISTS create_team CASCADE;
CREATE PROCEDURE create_team(
  authentication VARCHAR(64),
  password_hash VARCHAR(64),
  captain VARCHAR(64),
  description TEXT DEFAULT NULL,
  country VARCHAR(32) DEFAULT NULL
)
LANGUAGE plpgsql AS $$
BEGIN
  CALL create_authentication(authentication, password_hash);

  INSERT INTO team (authentication, description, country)
    VALUES (authentication, description, country);

  INSERT INTO challenger_in_team(challenger, team, is_captain)
    VALUES (captain, authentication, true);
END;
$$;

DROP PROCEDURE IF EXISTS create_user_account CASCADE;
CREATE PROCEDURE create_user_account(
  authentication VARCHAR(64),
  password_hash VARCHAR(64),
  email VARCHAR(256),
  photo BYTEA DEFAULT NULL
)
LANGUAGE plpgsql AS $$
BEGIN
  SET CONSTRAINTS fk_contact DEFERRED;
  CALL create_authentication(authentication, password_hash);

  INSERT INTO user_account (authentication, primary_contact, photo)
    VALUES (authentication, email, photo);

  INSERT INTO contact(email, user_account)
    VALUES (email, authentication);
END;
$$;

DROP PROCEDURE IF EXISTS create_challenger CASCADE;
CREATE PROCEDURE create_challenger(
  user_account VARCHAR(64),
  password_hash VARCHAR(64),
  email VARCHAR(256),
  photo BYTEA DEFAULT NULL,
  sex CHAR(1) DEFAULT NULL,
  date_birthday TIMESTAMP WITH TIME ZONE DEFAULT NULL,
  country VARCHAR(32) DEFAULT NULL
)
LANGUAGE plpgsql AS $$
BEGIN
  CALL create_user_account(user_account, password_hash, email, photo);

  INSERT INTO challenger (user_account, sex, date_birthday, country)
    VALUES (user_account, sex, date_birthday, country);
END;
$$;

DROP PROCEDURE IF EXISTS create_manager CASCADE;
CREATE PROCEDURE create_manager(
  user_account VARCHAR(64),
  password_hash VARCHAR(64),
  email VARCHAR(256),
  photo BYTEA DEFAULT NULL,
  admin BOOLEAN DEFAULT FALSE,
  moderator BOOLEAN DEFAULT FALSE,
  author BOOLEAN DEFAULT FALSE
)
LANGUAGE plpgsql AS $$
BEGIN
  IF NOT admin AND NOT moderator AND NOT author THEN
    RAISE 'create_manager requires at least one role';
  END IF;

  CALL create_user_account(user_account, password_hash, email, photo);

  INSERT INTO manager (user_account)
    VALUES (user_account);

  IF admin THEN
    INSERT INTO admin (manager)
      VALUES (user_account);
  END IF;

  IF moderator THEN
    INSERT INTO moderator (manager)
      VALUES (user_account);
  END IF;

  IF author THEN
    INSERT INTO author (manager)
      VALUES (user_account);
  END IF;
END;
$$;


--NOTE: Triggers

-- challenger_in_team one captain (if only the captain remains and then
-- decides to leave, the relationship shouldn't be removed, instead we will set
-- the deleted_at value.)

DROP FUNCTION IF EXISTS check_team_captain_after_insert CASCADE;
CREATE FUNCTION check_team_captain_after_insert()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
  IF NOT 1 = (
    SELECT COUNT(*) FROM challenger_in_team ct
    WHERE ct.team = NEW.team AND ct.is_captain
  ) THEN
    RAISE 'team should have exactly one captain';
  END IF;
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS check_team_captain_after_insert ON challenger_in_team CASCADE;
CREATE CONSTRAINT TRIGGER check_team_captain_after_insert
  AFTER INSERT ON challenger_in_team
  DEFERRABLE
  FOR EACH ROW
  EXECUTE PROCEDURE check_team_captain_after_insert();


-- challenger_in_team logs automatically created
DROP FUNCTION IF EXISTS manage_team_quit_and_join CASCADE;
CREATE FUNCTION manage_team_quit_and_join()
    RETURNS TRIGGER
    LANGUAGE plpgsql AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO challenger_in_team_logs (challenger, team, event)
        VALUES (NEW.challenger, NEW.team, 'joined'::challenger_team_event);
RETURN NEW;
ELSIF (TG_OP = 'DELETE') THEN
        INSERT INTO challenger_in_team_logs (challenger, team, event)
        VALUES (OLD.challenger, OLD.team, 'left'::challenger_team_event);
RETURN OLD;
END IF;
    RAISE 'Error logging challenger_in_team arrival or departure'; -- This code should never be reached
RETURN NULL;
END;
$$;

DROP TRIGGER IF EXISTS challenger_in_team_log ON challenger_in_team CASCADE;
CREATE TRIGGER challenger_in_team_log
    BEFORE INSERT OR DELETE ON challenger_in_team
    FOR EACH ROW
    EXECUTE FUNCTION manage_team_quit_and_join();

-- ctf date check (end after start and not before) (create/update)
DROP FUNCTION IF EXISTS check_dates CASCADE;
CREATE FUNCTION check_dates()
    RETURNS TRIGGER
    LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.ended_at IS NOT NULL AND NEW.ended_at < NEW.started_at THEN
        RAISE 'ended_at can not be before strated_at';
END IF;
RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS ctf_date_check ON ctf CASCADE;
CREATE CONSTRAINT TRIGGER ctf_date_check
    AFTER INSERT OR UPDATE OF started_at, ended_at ON ctf
    DEFERRABLE
    FOR EACH ROW
    EXECUTE FUNCTION check_dates();

-- challenge needs at least one category
DROP FUNCTION IF EXISTS check_categories CASCADE;
CREATE FUNCTION check_categories()
    RETURNS TRIGGER
    LANGUAGE plpgsql AS $$
BEGIN
    IF 1 > (
        SELECT COUNT(*) FROM challenge_category cc
        WHERE NEW.challenge = cc.challenge
    ) THEN RAISE 'the challenge has no categories';
END IF;
RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS challenge_categories_check ON challenge CASCADE;
CREATE CONSTRAINT TRIGGER challenge_categories_check
    AFTER INSERT ON challenge
    DEFERRABLE
    FOR EACH ROW
    EXECUTE FUNCTION check_categories();

-- vulnerability requires at least one challenge
DROP FUNCTION IF EXISTS check_vulnerabilities CASCADE;
CREATE FUNCTION check_vulnerabilities()
    RETURNS TRIGGER
    LANGUAGE plpgsql AS $$
BEGIN
    IF 1 > (
        SELECT COUNT(*) FROM challenge_has_vulnerability cv
        WHERE NEW.title = cv.challenge
    ) THEN RAISE 'the challenge has no vulnerabilities';
END IF;
RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS challenge_vulnerabilities_check ON challenge CASCADE;
CREATE CONSTRAINT TRIGGER challenge_vulnerabilities_check
    AFTER INSERT ON challenge
    DEFERRABLE
    FOR EACH ROW
    EXECUTE FUNCTION check_vulnerabilities();

-- team can't join a ctf unless the ctf status is 'ready' or 'in progress'

COMMIT;
ROLLBACK;
