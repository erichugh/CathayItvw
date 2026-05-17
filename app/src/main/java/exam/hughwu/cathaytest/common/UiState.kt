package exam.hughwu.cathaytest.common

/**
 * Marker interface for screen UI state.
 *
 * Convention:
 * - Use a single immutable data class per screen with sensible defaults.
 * - Provide `companion object { fun initial() = ... }`.
 */
interface UiState
