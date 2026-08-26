DROP INDEX IF EXISTS idx_material_versions_material_id;

DROP INDEX IF EXISTS idx_reader_progress_reader_id;

CREATE INDEX idx_reader_progress_reader_status_updated
    ON reader_progress(reader_id, completed, updated_at DESC);