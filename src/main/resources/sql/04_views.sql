SET search_path = ctfman;
BEGIN;

-- NOTE: bellow are a list of views that could be useful ?!

-- View team (name), challenger (name), challenge
DROP VIEW IF EXISTS team_challengers_challenges_details CASCADE;
CREATE OR REPLACE VIEW team_challengers_challenges_details AS
SELECT
    t.authentication AS team_name,
    c.user_account AS challenger_name,
    ch.title AS challenge_name,
    ch.score AS challenge_score,
    ch.flag AS challenge_flag,
    ch.max_attempts AS challenge_max_attempts,
    ch.author AS challenge_author
FROM team t
JOIN challenger_in_team cit
ON t.authentication = cit.team
JOIN challenger c
ON cit.challenger = c.user_account
JOIN challenger_takes_part_in_challenge ctpic
ON c.user_account = ctpic.challenger
JOIN challenge ch
ON ctpic.challenge = ch.title;

-- View team (name), challenger (name), challenge (name), flag_tried
DROP VIEW IF EXISTS challenger_challenge_flag_tried;
CREATE OR REPLACE VIEW challenger_challenge_flag_tried AS
SELECT
    v.team_name,
    v.challenger_name,
    v.challenge_name,
    f.flag
FROM team_challengers_challenges_details v
JOIN challenger_tries_flag f
ON v.challenge_name = f.challenge AND v.challenger_name = f.challenger;

-- View team (name), challenger (name), challenge (name), challenge (score) (from view1), hint (name), hint (score_to:remove), score_total (score challenge - hint)
--  -> this view is not needed to calculate the score_total - do we still need it ?
DROP VIEW IF EXISTS teams_challenger_challenge_hint_details;
CREATE OR REPLACE VIEW teams_challenger_challenge_hint_details AS
SELECT
    v.team_name AS team_name,
    v.challenger_name AS challenger_name,
    v.challenge_name AS challenge_name,
    v.challenge_score AS challenge_score,
    h.name AS hint_name,
    CASE
            WHEN cth.hint IS NOT NULL THEN h.score_to_remove
            ELSE 0
        END AS hint_penality,
    (v.challenge_score -
        CASE
            WHEN cth.hint IS NOT NULL THEN h.score_to_remove
            ELSE 0
        END) AS score_total,
    f.flag
FROM team_challengers_challenges_details v
LEFT JOIN hint h
ON v.challenge_name = h.challenge
LEFT JOIN challenger_took_hint cth
ON v.challenger_name = cth.challenger AND h.name = cth.hint
INNER JOIN challenger_challenge_flag_tried f
ON v.team_name = f.team_name AND v.challenger_name = f.challenger_name AND v.challenge_name = f.challenge_name
WHERE v.challenge_flag = f.flag;

-- View team, challenger,challenge, challenge_score, hint_penalty_total, score_total, challenge_flag
DROP VIEW IF EXISTS score_teams_details;
CREATE OR REPLACE VIEW score_teams_details AS
SELECT
    v.team_name,
    v.challenger_name,
    v.challenge_name,
    v.challenge_score,
    SUM(
        CASE
            WHEN cth.hint IS NOT NULL THEN h.score_to_remove
            ELSE 0
        END
    ) AS hint_penalty_total,
    (v.challenge_score -
        SUM(
            CASE
                WHEN cth.hint IS NOT NULL THEN h.score_to_remove
                ELSE 0
            END
        )
    ) AS score_total,
    f.flag AS challenge_flag
FROM team_challengers_challenges_details v
LEFT JOIN hint h
    ON v.challenge_name = h.challenge
LEFT JOIN challenger_took_hint cth
    ON v.challenger_name = cth.challenger AND h.name = cth.hint
INNER JOIN challenger_challenge_flag_tried f
    ON v.team_name = f.team_name
    AND v.challenger_name = f.challenger_name
    AND v.challenge_name = f.challenge_name
WHERE v.challenge_flag = f.flag
GROUP BY
    v.team_name,
    v.challenger_name,
    v.challenge_name,
    v.challenge_score,
    f.flag;

-- View ctf, team, score (the sum of all the highest score from a challenger for each challenge that a team took)
DROP VIEW IF EXISTS score_teams_ctf_details;
CREATE OR REPLACE VIEW score_teams_ctf_details AS
SELECT
    sign.ctf AS ctf_name,
    v.team_name,
    sign.created_at AS joined_at,
    SUM(
        CASE
            WHEN v.score_total = highest_team_score.highest_score THEN v.score_total
            ELSE 0
        END
    ) AS score_total
FROM score_teams_details v
JOIN team_sign_up_to_ctf sign
    ON sign.team = v.team_name
JOIN (
    SELECT
        team_name,
        MAX(score_total) AS highest_score
    FROM score_teams_details
    GROUP BY team_name
) highest_team_score
    ON v.team_name = highest_team_score.team_name
    AND v.score_total = highest_team_score.highest_score
JOIN challenge ch
    ON v.challenge_name = ch.title
    AND ch.ctf = sign.ctf
GROUP BY sign.ctf, v.team_name, sign.created_at;

-- View ongoing ctfs (ctfs that are in their start/end window) - do we need this view ?
DROP VIEW IF EXISTS ongoing_ctfs;
CREATE OR REPLACE VIEW ongoing_ctfs AS
SELECT
    ctf.title AS ctf_name,
    ctf.admin AS ctf_admin,
    ctf.localisation AS ctf_localisation,
    ctf.started_at AS ctf_started_at
FROM ctf
WHERE ctf.status = 'in progress';

-- View comment for the challenge written by the challenger and its parent; with their moderator
DROP VIEW IF EXISTS comments;
CREATE OR REPLACE VIEW comments AS
SELECT
    ch.moderator AS comment_moderator,
    c.challenge AS comment_for_challenge,
    c.message AS comment_message,
    c.challenger AS comment_writter,
    c.created_at AS comment_created_at,
    parent_comment.message AS comment_parent
FROM comment c
INNER JOIN challenge ch
ON c.challenge = ch.title
LEFT JOIN comment parent_comment
    ON c.parent = parent_comment.id;

COMMIT;
