CREATE OR REPLACE FUNCTION store_logged_in_user() RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM LoggedInUser;
    INSERT INTO LoggedInUser (userID, name)
    VALUES (NEW.userID, NEW.name);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER store_logged_in_user_trigger
AFTER INSERT ON Users
FOR EACH ROW
EXECUTE PROCEDURE store_logged_in_user();