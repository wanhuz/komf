ALTER TABLE MATCHED_SERIES
    RENAME TO MATCHED_SERIES_OLD;

CREATE TABLE MATCHED_SERIES
(
    SERIES_ID    varchar NOT NULL,
    SERVER_TYPE  varchar NOT NULL,
    THUMBNAIL_ID varchar,
    PRIMARY KEY (SERIES_ID, SERVER_TYPE)
);

INSERT INTO MATCHED_SERIES (SERIES_ID, THUMBNAIL_ID, SERVER_TYPE)
SELECT SERIES_ID, THUMBNAIL_ID, 'KOMGA'
FROM MATCHED_SERIES_OLD;

DROP TABLE MATCHED_SERIES_OLD;

ALTER TABLE MATCHED_BOOKS
    RENAME TO MATCHED_BOOKS_OLD;

CREATE TABLE MATCHED_BOOKS
(
    BOOK_ID      varchar NOT NULL,
    SERVER_TYPE  varchar NOT NULL,
    SERIES_ID    varchar NOT NULL,
    THUMBNAIL_ID varchar,
    PRIMARY KEY (BOOK_ID, SERVER_TYPE)
);

INSERT INTO MATCHED_BOOKS (BOOK_ID, SERIES_ID, THUMBNAIL_ID, SERVER_TYPE)
SELECT BOOK_ID, SERIES_ID, THUMBNAIL_ID, 'KOMGA'
FROM MATCHED_BOOKS_OLD;

DROP TABLE MATCHED_BOOKS_OLD;
