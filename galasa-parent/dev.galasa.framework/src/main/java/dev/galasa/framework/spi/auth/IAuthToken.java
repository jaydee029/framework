/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.auth;

import java.time.Instant;

/**
 * An interface for auth token beans to implement. This allows token properties to
 * be retrieved from different sources (e.g. the ID of a token can correspond to
 * the ID of a database record).
 */
public interface IAuthToken {

    String getTokenId();

    String getDescription();

    Instant getCreationTime();

    User getOwner();
}
