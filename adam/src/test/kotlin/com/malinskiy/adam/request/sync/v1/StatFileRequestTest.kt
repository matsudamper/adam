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

package com.malinskiy.adam.request.sync.v1

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.server.junit4.AdbServerRule
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.time.Instant


class StatFileRequestTest {
    @get:Rule
    val server = AdbServerRule()
    val client: AndroidDebugBridgeClient
        get() = server.client

    @Test
    fun testReturnsProperContent() {
        runBlocking {
            server.session {
                expectCmd { "host:transport:serial" }.accept()
                expectCmd { "sync:" }.accept()

                expectStat { "/sdcard/testfile" }
                respondStat(128, 0x744, 10000)
            }

            val output = client.execute(StatFileRequest("/sdcard/testfile"), serial = "serial")
            assertThat(output.mtime).isEqualTo(Instant.ofEpochSecond(10000))
            assertThat(output.mode).isEqualTo(0x744.toUInt())
            assertThat(output.size).isEqualTo(128.toUInt())
        }
    }
}
