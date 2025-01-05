DROP SCHEMA IF EXISTS ctfman CASCADE;
CREATE SCHEMA ctfman;
COMMENT ON SCHEMA ctfman IS 'Project BDR';

SET search_path = ctfman;

BEGIN;
--
-- Tables
--

CREATE TABLE authentication (
  identification VARCHAR(64) PRIMARY KEY,
  -- https://stackoverflow.com/questions/5881169/what-column-type-length-should-i-use-for-storing-a-bcrypt-hashed-password-in-a-d
  password_hash VARCHAR(64) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
  deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE user_account (
  authentication VARCHAR(64) PRIMARY KEY,
  primary_contact VARCHAR(256) NOT NULL,
  photo BYTEA
);

CREATE TABLE contact (
-- https://stackoverflow.com/questions/386294/what-is-the-maximum-length-of-a-valid-email-address
  email VARCHAR(256) UNIQUE,
  user_account VARCHAR(64),
  PRIMARY KEY(email, user_account)
);

CREATE TYPE SOCIAL_TYPE AS ENUM(
  'x',
  'bluesky',
  'reddit',
  'discord',
  'domain',
  'blog',
  'youtube',
  'facebook',
  'twitch',
  'github',
  'gitlab',
  'patreon'
  -- 'onlyFans'
);

CREATE TABLE social_account (
  handle VARCHAR(256),
  user_account VARCHAR(64),
  type SOCIAL_TYPE,
  PRIMARY KEY(handle, user_account, type)
);

CREATE TABLE challenger (
  user_account VARCHAR(64) PRIMARY KEY,
  sex CHAR(1),
  date_birthday TIMESTAMP WITH TIME ZONE,
  country VARCHAR(32)
);

CREATE TABLE manager (
  user_account VARCHAR(64) PRIMARY KEY
);

CREATE TABLE admin (
  manager VARCHAR(64) PRIMARY KEY
);

CREATE TABLE moderator (
  manager VARCHAR(64) PRIMARY KEY
);

CREATE TABLE author (
  manager VARCHAR(64) PRIMARY KEY
);

CREATE TABLE team (
  authentication VARCHAR(64) PRIMARY KEY,
  description TEXT,
  -- seems like 28 should be enough
  country VARCHAR(32)
);

CREATE TABLE challenger_in_team (
  challenger VARCHAR(64),
  team VARCHAR(64),
  is_captain BOOLEAN DEFAULT false NOT NULL,
  PRIMARY KEY(challenger, team)
);

CREATE TYPE CHALLENGER_TEAM_EVENT AS ENUM(
  'joined',
  'left'
);

CREATE TABLE challenger_in_team_logs (
  challenger VARCHAR(64),
  team VARCHAR(64),
  event CHALLENGER_TEAM_EVENT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
  PRIMARY KEY(challenger, team, created_at)
);

CREATE TYPE CTF_STATUS AS ENUM('wip','ready','in progress','finished');

CREATE TABLE ctf (
    title VARCHAR(256) PRIMARY KEY,
    admin VARCHAR(64) NOT NULL,
    -- Might be a bit too much
    description TEXT DEFAULT 'TO BE IMPLEMENTED' NOT NULL,
    localisation VARCHAR(256) NOT NULL,
    status CTF_STATUS DEFAULT 'wip' NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE,
    ended_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE author_access_ctf (
  author VARCHAR(64),
  ctf VARCHAR(256),
  PRIMARY KEY(author, ctf)
);

CREATE TABLE team_sign_up_to_ctf (
  team VARCHAR(256),
  ctf VARCHAR(256),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
  PRIMARY KEY(team, ctf)
);

CREATE TYPE DIFFICULTY AS ENUM (
  'easy',
  'medium',
  'hard',
  'expert'
);

CREATE TABLE challenge (
  title VARCHAR(256) PRIMARY KEY,
  ctf VARCHAR(256) NOT NULL,
  author VARCHAR(64) NOT NULL,
  moderator VARCHAR(64) NOT NULL,
  description TEXT DEFAULT 'TO BE IMPLEMENTED' NOT NULL,
  difficulty DIFFICULTY NOT NULL,
  score SMALLINT NOT NULL,
  flag VARCHAR(128) NOT NULL,
  max_attempts SMALLINT,
  started_at TIMESTAMP WITH TIME ZONE,
  ended_at TIMESTAMP WITH TIME ZONE
);

CREATE TYPE CATEGORY AS ENUM(
  'crypto',
  'web',
  'docker',
  'privilege escalation',
  'osint',
  'password'
);

CREATE TABLE challenge_category (
  category CATEGORY,
  challenge VARCHAR(256),
  PRIMARY KEY(category, challenge)
);

CREATE TABLE vulnerability (
  cve VARCHAR(32) PRIMARY KEY,
  description TEXT NOT NULL
);

CREATE TABLE challenge_has_vulnerability (
  challenge VARCHAR(256),
  vulnerability VARCHAR(32),
  PRIMARY KEY(challenge, vulnerability)
);

CREATE TABLE hint (
  name VARCHAR(64),
  challenge VARCHAR(256),
  body TEXT,
  level SMALLINT NOT NULL,
  score_to_remove SMALLINT NOT NULL,
  PRIMARY KEY(name, challenge)
);

CREATE TABLE challenger_took_hint (
    challenger VARCHAR(64),
    hint VARCHAR(64),
    challenge VARCHAR(256),
    taken_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
    PRIMARY KEY (challenger, hint, challenge)
);

CREATE TABLE tool (
  name VARCHAR(64) PRIMARY KEY,
  description TEXT NOT NULL,
  url VARCHAR(256)
);

CREATE TYPE PLATFORM AS ENUM(
  'windows',
  'linux',
  'macos',
  'android',
  'ios'
);

CREATE TABLE tool_platform (
  tool VARCHAR(64),
  platform PLATFORM,
  PRIMARY KEY(tool, platform)
);

CREATE TABLE challenge_recommends_tool (
  challenge VARCHAR(256),
  tool VARCHAR(64),
  PRIMARY KEY(challenge, tool)
);

CREATE TABLE challenger_takes_part_in_challenge (
  challenger VARCHAR(64),
  challenge VARCHAR(256),
  write_up BYTEA,
  PRIMARY KEY(challenger, challenge)
);

CREATE TABLE challenger_tries_flag (
  challenger VARCHAR(64),
  challenge VARCHAR(256),
  flag VARCHAR(128),
  PRIMARY KEY(challenger, challenge, flag)
);

CREATE TABLE comment (
    id SERIAL PRIMARY KEY,
    message VARCHAR(512) NOT NULL,
    challenge VARCHAR(256) NOT NULL,
    challenger VARCHAR(64) NOT NULL,
    parent INTEGER, -- Cannot be SERIAL since it would be auto incremented
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

CREATE TABLE tag (
  name VARCHAR(64) PRIMARY KEY
);

CREATE TABLE comment_has_tag (
  comment INTEGER,
  tag VARCHAR(64),
  PRIMARY KEY(comment, tag)
);

-- SESSION table, used to persist the user session
CREATE TABLE session (
  token VARCHAR(256) PRIMARY KEY,
  user_account VARCHAR(64) NOT NULL,
  expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
  ip_address VARCHAR(45),
  user_agent TEXT
);

COMMIT; -- End tables


BEGIN;

--
-- Foreign Keys
--

ALTER TABLE user_account
  ADD CONSTRAINT fk_authentication
    FOREIGN KEY (authentication)
    REFERENCES authentication(identification)
    ON UPDATE CASCADE
    -- If the authentication gets deleted it makes sense that its specialization
    -- is also deleted. In any cases, the application shouldn't provide a way to
    -- delete an authentication directly, this is mostly for maintenance.
    ON DELETE CASCADE
    DEFERRABLE,
  ADD CONSTRAINT fk_contact
    FOREIGN KEY (authentication, primary_contact)
    REFERENCES contact(user_account, email)
    ON UPDATE CASCADE
    -- We don't want the user to be deleted if we try to remove his primary email
    ON DELETE RESTRICT
    DEFERRABLE;

ALTER TABLE contact
  ADD CONSTRAINT fk_user_account
    FOREIGN KEY (user_account)
    REFERENCES user_account(authentication)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE social_account
  ADD CONSTRAINT fk_user_account
    FOREIGN KEY (user_account)
    REFERENCES user_account(authentication)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE challenger
  ADD CONSTRAINT fk_user_account
    FOREIGN KEY (user_account)
    REFERENCES user_account(authentication)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE manager
  ADD CONSTRAINT fk_user_account
    FOREIGN KEY (user_account)
    REFERENCES user_account(authentication)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE admin
  ADD CONSTRAINT fk_manager
    FOREIGN KEY (manager)
    REFERENCES manager(user_account)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE moderator
  ADD CONSTRAINT fk_manager
    FOREIGN KEY (manager)
    REFERENCES manager(user_account)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE author
  ADD CONSTRAINT fk_manager
    FOREIGN KEY (manager)
    REFERENCES manager(user_account)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE team
  ADD CONSTRAINT fk_authentication
    FOREIGN KEY (authentication)
    REFERENCES authentication(identification)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE challenger_in_team
  ADD CONSTRAINT fk_challenger
    FOREIGN KEY (challenger)
    REFERENCES challenger(user_account)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE,
  ADD CONSTRAINT fk_team
    FOREIGN KEY (team)
    REFERENCES team(authentication)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE challenger_in_team_logs
  ADD CONSTRAINT fk_challenger
    FOREIGN KEY (challenger)
    REFERENCES challenger(user_account)
    ON UPDATE CASCADE
    -- this means losing logs but as stated previously, we shouldn't delete
    -- authentication at all.
    ON DELETE CASCADE
    DEFERRABLE,
  ADD CONSTRAINT fk_team
    FOREIGN KEY (team)
    REFERENCES team(authentication)
    ON UPDATE CASCADE
    -- this means losing logs but as stated previously, we shouldn't delete
    -- authentication at all.
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE ctf
  ADD CONSTRAINT fk_admin
    FOREIGN KEY (admin)
    REFERENCES admin(manager)
    ON UPDATE CASCADE
    -- Should we? We can't exactly set to null since this is NOT NULL.
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE author_access_ctf
  ADD CONSTRAINT fk_ctf
    FOREIGN KEY (ctf)
    REFERENCES ctf(title)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE,
  ADD CONSTRAINT fk_author
    FOREIGN KEY (author)
    REFERENCES author(manager)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE team_sign_up_to_ctf
  ADD CONSTRAINT fk_ctf
    FOREIGN KEY (ctf)
    REFERENCES ctf(title)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE,
  ADD CONSTRAINT fk_team
    FOREIGN KEY (team)
    REFERENCES team(authentication)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE challenge
  ADD CONSTRAINT fk_ctf
    FOREIGN KEY (ctf)
    REFERENCES ctf(title)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE,
  ADD CONSTRAINT fk_author
    FOREIGN KEY (author)
    REFERENCES author(manager)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE,
  ADD CONSTRAINT fk_moderator
    FOREIGN KEY (moderator)
    REFERENCES moderator(manager)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE challenge_category
  ADD CONSTRAINT fk_challenge
    FOREIGN KEY (challenge)
    REFERENCES challenge(title)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE challenge_has_vulnerability
  ADD CONSTRAINT fk_challenge
    FOREIGN KEY (challenge)
    REFERENCES challenge(title)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE,
  ADD CONSTRAINT fk_vulnerability
    FOREIGN KEY (vulnerability)
    REFERENCES vulnerability(cve)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE hint
  ADD CONSTRAINT fk_challenge
    FOREIGN KEY (challenge)
    REFERENCES challenge(title)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE challenger_took_hint
    ADD CONSTRAINT fk_challenger
        FOREIGN KEY (challenger)
        REFERENCES challenger(user_account)
        ON UPDATE CASCADE
        ON DELETE CASCADE
        DEFERRABLE,
    ADD CONSTRAINT fk_hint
        FOREIGN KEY (hint, challenge)
        REFERENCES hint(name, challenge)
        ON UPDATE CASCADE
        ON DELETE CASCADE
        DEFERRABLE;

ALTER TABLE tool_platform
  ADD CONSTRAINT fk_tool
    FOREIGN KEY (tool)
    REFERENCES tool(name)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE challenge_recommends_tool
  ADD CONSTRAINT fk_challenge
    FOREIGN KEY (challenge)
    REFERENCES challenge(title)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE,
  ADD CONSTRAINT fk_tool
    FOREIGN KEY (tool)
    REFERENCES tool(name)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE challenger_takes_part_in_challenge
  ADD CONSTRAINT fk_challenge
    FOREIGN KEY (challenge)
    REFERENCES challenge(title)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE,
  ADD CONSTRAINT fk_challenger
    FOREIGN KEY (challenger)
    REFERENCES challenger(user_account)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE challenger_tries_flag
  ADD CONSTRAINT fk_takes_part_in
    FOREIGN KEY (challenger, challenge)
    REFERENCES challenger_takes_part_in_challenge(challenger, challenge)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE comment
  ADD CONSTRAINT fk_challenge
    FOREIGN KEY (challenge)
    REFERENCES challenge(title)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE,
  ADD CONSTRAINT fk_parent
    FOREIGN KEY (parent)
    REFERENCES comment(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE comment_has_tag
  ADD CONSTRAINT fk_comment
    FOREIGN KEY (comment)
    REFERENCES comment(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    DEFERRABLE,
  ADD CONSTRAINT fk_tag
    FOREIGN KEY (tag)
    REFERENCES tag(name)
    ON UPDATE CASCADE
    -- Most likely won't be triggered from that side, but it doesn't make sense
    -- to restrict or set null
    ON DELETE CASCADE
    DEFERRABLE;

ALTER TABLE session
  ADD CONSTRAINT fk_user
    FOREIGN KEY (user_account)
    REFERENCES user_account(authentication)
    ON UPDATE CASCADE -- most likely OK
    ON DELETE CASCADE
    DEFERRABLE;

COMMIT; -- End foreign keys
ROLLBACK;
