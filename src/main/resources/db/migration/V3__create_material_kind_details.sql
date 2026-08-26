CREATE TABLE pdf_details (
                             version_id BIGINT PRIMARY KEY,

                             page_count INTEGER NOT NULL,

                             file_url TEXT NOT NULL,

                             CONSTRAINT fk_pdf_details_version
                                 FOREIGN KEY (version_id)
                                     REFERENCES material_versions(id)
                                     ON DELETE RESTRICT,

                             CONSTRAINT chk_pdf_page_count_positive
                                 CHECK (page_count > 0),

                             CONSTRAINT chk_pdf_file_url_not_blank
                                 CHECK (char_length(trim(file_url)) > 0)
);


CREATE TABLE article_details (
                                 version_id BIGINT PRIMARY KEY,

                                 word_count INTEGER NOT NULL,

                                 CONSTRAINT fk_article_details_version
                                     FOREIGN KEY (version_id)
                                         REFERENCES material_versions(id)
                                         ON DELETE RESTRICT,

                                 CONSTRAINT chk_article_word_count_non_negative
                                     CHECK (word_count >= 0)
);


CREATE TABLE book_details (
                              version_id BIGINT PRIMARY KEY,

                              author VARCHAR(255) NOT NULL,

    /*
     * The assignment requires a list of chapters.
     *
     * We store the actual chapter list rather than a chapter count.
     */
                              chapters JSONB NOT NULL,

                              CONSTRAINT fk_book_details_version
                                  FOREIGN KEY (version_id)
                                      REFERENCES material_versions(id)
                                      ON DELETE RESTRICT,

                              CONSTRAINT chk_book_author_not_blank
                                  CHECK (char_length(trim(author)) > 0),

                              CONSTRAINT chk_book_chapters_array
                                  CHECK (
                                      jsonb_typeof(chapters) = 'array'
                                          AND jsonb_array_length(chapters) > 0
                                      )
);


CREATE TABLE slide_deck_details (
                                    version_id BIGINT PRIMARY KEY,

                                    slide_count INTEGER NOT NULL,

                                    file_url TEXT NOT NULL,

                                    CONSTRAINT fk_slide_deck_details_version
                                        FOREIGN KEY (version_id)
                                            REFERENCES material_versions(id)
                                            ON DELETE RESTRICT,

                                    CONSTRAINT chk_slide_count_positive
                                        CHECK (slide_count > 0),

                                    CONSTRAINT chk_slide_deck_file_url_not_blank
                                        CHECK (char_length(trim(file_url)) > 0)
);