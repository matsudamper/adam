/*
 * Copyright (C) 2021 Anton Malinskiy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.malinskiy.adam.request.misc

import com.malinskiy.adam.Const
import com.malinskiy.adam.request.ComplexRequest
import com.malinskiy.adam.request.HostTarget
import com.malinskiy.adam.transport.AndroidReadChannel
import com.malinskiy.adam.transport.AndroidWriteChannel
import java.nio.ByteBuffer

/**
 * Disconnects a device previously connected using ConnectDeviceRequest
 */
class DisconnectDeviceRequest(
    val host: String,
    val port: Int = 5555
) : ComplexRequest<String>(target = HostTarget) {

    override fun serialize() = createBaseRequest("disconnect:$host:$port")

    override suspend fun readElement(readChannel: AndroidReadChannel, writeChannel: AndroidWriteChannel): String {
        val sizeBuffer: ByteBuffer = ByteBuffer.allocate(4)
        readChannel.readFully(sizeBuffer)
        sizeBuffer.rewind()

        val size = String(sizeBuffer.array(), Const.DEFAULT_TRANSPORT_ENCODING).toInt(radix = 16)

        val payloadBuffer = ByteBuffer.allocate(size)
        readChannel.readFully(payloadBuffer)
        payloadBuffer.rewind()
        return String(payloadBuffer.array(), Const.DEFAULT_TRANSPORT_ENCODING)
    }
}
