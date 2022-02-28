/*
 * Copyright (c) 2022 Redlink GmbH.
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

package io.redlink.nlp.time.duckling;

import io.redlink.nlp.model.temporal.Temporal;

/**
 * DateValue was initially used as values for the start/end
 * date/times for DateToken. Those do now use the
 * Temporal class. This is only here for backward compatibility reasons
 *
 * @author Rupert Westenthaler
 * @deprecated Use {@link Temporal} instead
 */
@Deprecated
public class DateValue extends io.redlink.nlp.model.temporal.Temporal {


}
