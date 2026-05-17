package exam.hughwu.cathaytest.common

/**
 * Marker interface for user intents.
 *
 * Convention:
 * - Sealed class per screen, `object` for parameterless intents,
 *   `data class` when carrying payload.
 * - Avoid leaking Android types (Context, View) here — keep this layer pure.
 */
interface UiIntent
