/*
 * Copyright (c) 2016-2023 The gRPC-Spring Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.devh.boot.grpc.client.nameresolver;

import io.grpc.NameResolver;
import io.grpc.Status;
import lombok.Getter;

@Getter
public class TestableListener extends NameResolver.Listener2 {

    private NameResolver.ResolutionResult result;
    private Status error;
    private boolean resultWasSet = false;
    private boolean errorWasSet = false;

    @Override
    public void onResult(NameResolver.ResolutionResult resolutionResult) {
        this.result = resolutionResult;
        resultWasSet = true;
    }

    @Override
    public void onError(Status error) {
        this.error = error;
        errorWasSet = true;
    }

}
