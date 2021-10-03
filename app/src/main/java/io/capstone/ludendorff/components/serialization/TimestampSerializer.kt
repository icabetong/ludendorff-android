package io.capstone.ludendorff.components.serialization

import com.google.firebase.Timestamp
import io.capstone.ludendorff.features.asset.Asset
import kotlinx.serialization.KSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json

object TimestampSerializer: KSerializer<Timestamp> {
    private const val FIELD_NANOSECONDS = "_nanoseconds"
    private const val FIELD_SECONDS = "_seconds"

    override fun deserialize(decoder: Decoder): Timestamp {
        return decoder.decodeStructure(descriptor) {
            Timestamp (
                decodeLongElement(descriptor, 0),
                decodeIntElement(descriptor, 1)
            )
        }
    }

    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("Timestamp") {
            element<Long>(FIELD_SECONDS)
            element<Int>(FIELD_NANOSECONDS)
        }

    override fun serialize(encoder: Encoder, value: Timestamp) {
        encoder.encodeStructure(descriptor) {
            encodeLongElement(descriptor, 0, value.seconds)
            encodeIntElement(descriptor, 1, value.nanoseconds)
        }
    }

}