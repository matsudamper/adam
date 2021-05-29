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

package com.malinskiy.adam.request.device

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.malinskiy.adam.Const
import com.malinskiy.adam.request.Feature
import com.malinskiy.adam.server.stub.StubSocket
import com.malinskiy.adam.transport.use
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FetchDeviceFeaturesRequestTest {
    @Test
    fun testSerialization() {
        val fetchDeviceFeaturesRequest = FetchDeviceFeaturesRequest("cafebabe")

        assertThat(
            String(
                fetchDeviceFeaturesRequest.serialize(),
                Const.DEFAULT_TRANSPORT_ENCODING
            )
        ).isEqualTo("001Dhost-serial:cafebabe:features")
    }

    @Test
    fun testResponse() {
        val fetchDeviceFeaturesRequest = FetchDeviceFeaturesRequest("cafebabe")
        runBlocking {
            StubSocket(
                content = "0054fixed_push_symlink_timestamp,apex,fixed_push_mkdir,stat_v2,abb_exec,cmd,abb,shell_v2"
                    .toByteArray(Const.DEFAULT_TRANSPORT_ENCODING)
            ).use { socket ->
                val features = fetchDeviceFeaturesRequest.readElement(socket)

                assertThat(features).containsExactly(
                    Feature.FIXED_PUSH_SYMLINK_TIMESTAMP,
                    Feature.APEX,
                    Feature.FIXED_PUSH_MKDIR,
                    Feature.STAT_V2,
                    Feature.ABB_EXEC,
                    Feature.CMD,
                    Feature.ABB,
                    Feature.SHELL_V2
                )
            }
        }
    }
}
