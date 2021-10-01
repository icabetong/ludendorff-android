package io.capstone.ludendorff.components.serialization

import io.capstone.ludendorff.features.asset.Asset
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object StatusSerializer: KSerializer<Asset.Status> {
    override fun deserialize(decoder: Decoder): Asset.Status {
        return Asset.Status.parse(decoder.decodeString())
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(Asset.FIELD_STATUS, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Asset.Status) {
        encoder.encodeString(value.toString())
    }

}