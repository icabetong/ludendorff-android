package io.capstone.ludendorff.components.serialization

import com.google.firebase.Timestamp
import io.capstone.ludendorff.features.asset.Asset
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

object TimestampSerializer: KSerializer<Timestamp> {
    private const val FIELD_NANOSECONDS = "_nanoseconds"
    private const val FIELD_SECONDS = "_seconds"

    override fun deserialize(decoder: Decoder): Timestamp {
        return decoder.decodeStructure(descriptor) {
            Timestamp(
                decodeLongElement(PrimitiveSerialDescriptor(FIELD_SECONDS, PrimitiveKind.LONG), 0),
                decodeIntElement(PrimitiveSerialDescriptor(FIELD_NANOSECONDS, PrimitiveKind.INT), 1)
            )
        }
    }

    override val descriptor: SerialDescriptor
        = buildClassSerialDescriptor(Asset.FIELD_DATE_CREATED) {
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