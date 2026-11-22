package io.github.samuolis.domposable.rive

/** A named animation or state machine rendered by the platform's Rive runtime. */
data class DomposableRiveProps(
    val file: ByteArray,
    val artboardName: String? = null,
    val animationName: String? = null,
    val stateMachineName: String? = null,
    val autoplay: Boolean = true,
    val playbackKey: Long = 0,
    val contentDescription: String? = null,
) {
    init {
        require(animationName == null || stateMachineName == null) {
            "Choose either a Rive animation or a state machine."
        }
    }
}
