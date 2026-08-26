/*
 * Critical assignment requirement:
 *
 * At most one LIVE version for a material.
 *
 * This is enforced by PostgreSQL itself.
 */
CREATE UNIQUE INDEX ux_one_live_version_per_material
    ON material_versions(material_id)
    WHERE status = 'LIVE';


/*
 * Used when finding versions for a material.
 *
 * The UNIQUE(material_id, version_number) constraint already creates
 * an index useful for version-number lookups, but this index is useful
 * for material-level version/status operations where ordering/version
 * number is not part of the lookup.
 */
CREATE INDEX idx_material_versions_material_id
    ON material_versions(material_id);


/*
 * Helpful for catalog queries filtering by kind.
 */
CREATE INDEX idx_materials_kind
    ON materials(kind);


/*
 * PDF search required by the assignment:
 *
 * "Show me every PDF with more than 100 pages."
 */
CREATE INDEX idx_pdf_details_page_count
    ON pdf_details(page_count);


/*
 * Reader's own list:
 *
 * Find all progress records belonging to a reader.
 */
CREATE INDEX idx_reader_progress_reader_id
    ON reader_progress(reader_id);