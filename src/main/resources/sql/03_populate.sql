SET search_path = ctfman;
BEGIN;
SET CONSTRAINTS ALL DEFERRED;

-- TODO: fill the database with dummy data
--rollback;
-- Insert data into the `authentication` table
INSERT INTO authentication (identification, password_hash) VALUES
('user1', '$2a$04$9SJZCvbCnXwVEfqXezEHsuEMEMDv7CS1JYmH7yfzVIaHCs4UsymrG'),
('user2', '$2a$04$9SJZCvbCnXwVEfqXezEHsuEMEMDv7CS1JYmH7yfzVIaHCs4UsymrG'),
('user3', '$2a$04$9SJZCvbCnXwVEfqXezEHsuEMEMDv7CS1JYmH7yfzVIaHCs4UsymrG'),
('user4', '$2a$04$9SJZCvbCnXwVEfqXezEHsuEMEMDv7CS1JYmH7yfzVIaHCs4UsymrG'),
('user5', '$2a$04$9SJZCvbCnXwVEfqXezEHsuEMEMDv7CS1JYmH7yfzVIaHCs4UsymrG'),
('team1', '$2a$04$9SJZCvbCnXwVEfqXezEHsuEMEMDv7CS1JYmH7yfzVIaHCs4UsymrG'),
('team2', '$2a$04$9SJZCvbCnXwVEfqXezEHsuEMEMDv7CS1JYmH7yfzVIaHCs4UsymrG'),
('supadmin', '$2a$04$9SJZCvbCnXwVEfqXezEHsuEMEMDv7CS1JYmH7yfzVIaHCs4UsymrG'),
('admin1', '$2a$04$9SJZCvbCnXwVEfqXezEHsuEMEMDv7CS1JYmH7yfzVIaHCs4UsymrG'),
('moderator1', '$2a$04$9SJZCvbCnXwVEfqXezEHsuEMEMDv7CS1JYmH7yfzVIaHCs4UsymrG'),
('author1', '$2a$04$9SJZCvbCnXwVEfqXezEHsuEMEMDv7CS1JYmH7yfzVIaHCs4UsymrG');

-- Insert data into the `contact` table
INSERT INTO contact (email, user_account) VALUES
('user1@example.com', 'user1'),
('user2@example.com', 'user2'),
('user3@example.com', 'user3'),
('user4@example.com', 'user4'),
('user5@example.com', 'user5'),
('supadmin@example.com', 'supadmin'),
('admin1@example.com', 'admin1'),
('moderator1@example.com', 'moderator1'),
('author1@example.com', 'author1');

-- Insert data into the `user_account` table
INSERT INTO user_account (authentication, primary_contact) VALUES
('user1', 'user1@example.com'),
('user2', 'user2@example.com'),
('user3', 'user3@example.com'),
('user4', 'user4@example.com'),
('user5', 'user5@example.com'),
('supadmin', 'supadmin@example.com'),
('admin1', 'admin1@example.com'),
('moderator1', 'moderator1@example.com'),
('author1', 'author1@example.com');

-- Insert data into the `social_account` table
INSERT INTO social_account (handle, user_account, type) VALUES
('@user1', 'user1', 'x'),
('https://my-domain.ch', 'user2', 'domain');

-- Insert data into the `team` table
INSERT INTO team (authentication) VALUES
('team1'),
('team2');

-- Insert data into the `challenger` table
INSERT INTO challenger (user_account) VALUES
('user1'),
('user3'),
('user4'),
('user5'),
('user2');

-- Insert data into the `manager` table
INSERT INTO manager (user_account) VALUES
('supadmin'),
('admin1'),
('moderator1'),
('author1');

-- Insert data into the `admin` table
INSERT INTO admin (manager) VALUES
('supadmin'),
('admin1');

-- Insert data into the `moderator` table
INSERT INTO moderator (manager) VALUES
('supadmin'),
('moderator1');

-- Insert data into the `author` table
INSERT INTO author (manager) VALUES
('supadmin'),
('author1');

-- Insert data into the `challenger_in_team` table
INSERT INTO challenger_in_team (challenger, team, is_captain) VALUES
('user1', 'team1', true),
('user2', 'team1', false),
('user5', 'team1', false),
('user4', 'team2', false),
('user3', 'team2', true);

-- Insert data into the `ctf` table
INSERT INTO ctf (title, admin, localisation, status) VALUES
('CTF 1', 'admin1', 'Internet', 'in progress'),
('CTF 2', 'supadmin', 'Internet', 'in progress');

-- Insert data into the `team_sign_up_to_ctf` table
INSERT INTO team_sign_up_to_ctf (team, ctf) VALUES
('team1', 'CTF 1'),
('team2', 'CTF 1'),
('team2', 'CTF 2'),
('team1', 'CTF 2');

-- Insert data into the `challenge` table
INSERT INTO challenge (title, ctf, author, moderator, difficulty, score, flag) VALUES
('Challenge 1', 'CTF 1', 'author1', 'moderator1', 'easy', 100, 'flag{hello world!}'),
('Challenge 2', 'CTF 1', 'author1', 'moderator1', 'easy', 100, 'flag{P@$$w0rd!}'),
('I am lost', 'CTF 2', 'author1', 'moderator1', 'easy', 100, 'flag{P@$$w0rd!}');

-- Insert data into the `challenge_category` table
INSERT INTO challenge_category (category, challenge) VALUES
('crypto', 'Challenge 1'),
('password', 'Challenge 2'),
('osint', 'I am lost');

-- Insert data into the `vulnerability` table
INSERT INTO vulnerability (cve, description) VALUES
('CVE-2024-0001', 'Example vulnerability description');

-- Insert data into the `challenge_has_vulnerability` table
INSERT INTO challenge_has_vulnerability (challenge, vulnerability) VALUES
('Challenge 1', 'CVE-2024-0001');

-- Insert data into the `hint` table
INSERT INTO hint (name, challenge, body, level, score_to_remove) VALUES
('Hint 1', 'Challenge 1', 'some hint', 1, 20),
('Cookie', 'I am lost', 'some hint', 2, 30),
('Come on', 'I am lost', 'some hint', 1, 10);

-- Insert date into the `challenger_took_hint` table
INSERT INTO challenger_took_hint (challenger, hint, challenge) VALUES
('user1', 'Hint 1', 'Challenge 1'),
('user4', 'Cookie', 'I am lost'),
('user5', 'Come on', 'I am lost');

-- Insert data into the `tool` table
INSERT INTO tool (name, description, url) VALUES
('Tool 1', 'This is a tool description.', 'http://example.com/tool1');

-- Insert data into the `tool_platform` table
INSERT INTO tool_platform (tool, platform) VALUES
('Tool 1', 'linux');

-- Insert data into the `challenge_recommends_tool` table
INSERT INTO challenge_recommends_tool (challenge, tool) VALUES
('Challenge 1', 'Tool 1');

-- Insert data into the `challenger_takes_part_in_challenge` table
INSERT INTO challenger_takes_part_in_challenge (challenger, challenge) VALUES
('user1', 'Challenge 1'),
('user3', 'Challenge 1'),
('user3', 'Challenge 2'),
('user4', 'I am lost'),
('user5', 'I am lost'),
('user1', 'I am lost');

-- Insert data into the `challenger_tries_flag` table
INSERT INTO challenger_tries_flag (challenger, challenge, flag) VALUES
('user1', 'Challenge 1', 'flag{example}'),
('user3', 'Challenge 1', 'flag{test}'),
('user3', 'Challenge 2', 'flag{again}'),
('user1', 'I am lost', 'flag{password}'),
('user1', 'I am lost', 'flag{P@$$w0rd!}'),
('user4', 'I am lost', 'flag{password}'),
('user4', 'I am lost', 'flag{P@$$w0rd!}'),
('user5', 'I am lost', 'flag{idontknow}'),
('user5', 'I am lost', 'flag{P@$$w0rd!}');

-- Insert data into the `tag` table
INSERT INTO tag (name) VALUES
('Tag 1'), ('Tag 2');

WITH row AS (
  -- Insert data into the `comment` table
  INSERT INTO comment (message, challenge, challenger, parent) VALUES
  ('Great challenge!', 'Challenge 1', 'user1', null),
  ('Not is not', 'Challenge 1', 'user2', 1)
  RETURNING id
)
-- Insert data into the `comment_has_tag` table
INSERT INTO comment_has_tag (comment, tag)
SELECT row.id, tag
FROM row, (VALUES( 'Tag 1', 'Tag 2')) AS tags(tag);

COMMIT;
