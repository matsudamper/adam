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

package com.malinskiy.adam.junit4.rule

import androidx.test.platform.app.InstrumentationRegistry
import com.android.emulator.control.EmulatorControllerGrpcKt
import com.malinskiy.adam.junit4.android.contract.TestRunnerContract
import com.malinskiy.adam.junit4.android.rule.Mode
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import org.junit.Assert
import org.junit.Assume
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.concurrent.TimeUnit

class EmulatorRule(val mode: Mode = Mode.ASSERT) : TestRule {
    lateinit var grpc: EmulatorControllerGrpcKt.EmulatorControllerCoroutineStub
    private var channel: ManagedChannel? = null

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                val arguments = InstrumentationRegistry.getArguments()
                val grpcPort = arguments.getString(TestRunnerContract.grpcPortArgumentName)?.toIntOrNull()

                if (grpcPort != null) {
                    val localChannel = ManagedChannelBuilder.forAddress("localhost", grpcPort).apply {
                        usePlaintext()
                        executor(Dispatchers.IO.asExecutor())
                    }.build()
                    channel = localChannel
                    grpc = EmulatorControllerGrpcKt.EmulatorControllerCoroutineStub(localChannel)
                } else {
                    when (mode) {
                        Mode.SKIP -> return
                        Mode.ASSUME -> Assume.assumeTrue("No access to emulator's gRPC port has been provided", false)
                        Mode.ASSERT -> Assert.assertTrue("No access to emulator's gRPC port has been provided", false)
                    }
                }

                try {
                    base.evaluate()
                } finally {
                    channel?.shutdownNow()
                    channel?.awaitTermination(5, TimeUnit.SECONDS)
                }
            }
        }
    }
}
