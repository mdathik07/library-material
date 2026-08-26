CREATE TABLE video_details (
                               id BIGSERIAL PRIMARY KEY,
                               material_version_id BIGINT NOT NULL,
                               duration_minutes INTEGER NOT NULL,
                               transcript_available BOOLEAN NOT NULL,
                               video_url VARCHAR(1000) NOT NULL,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                               CONSTRAINT fk_video_details_version FOREIGN KEY (material_version_id)
                                   REFERENCES material_versions(id) ON DELETE RESTRICT
);

CREATE INDEX idx_video_details_version
    ON video_details(material_version_id);

-- Optional: Add unique constraint if you want one video per version
ALTER TABLE video_details
    ADD CONSTRAINT uq_video_one_per_version UNIQUE (material_version_id);
