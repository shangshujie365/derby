/*

   Derby - Class org.apache.derby.impl.sql.conn.SQLSessionContextImpl

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.apache.derby.impl.sql.conn;

import java.util.HashMap;
import org.apache.derby.iapi.sql.conn.SQLSessionContext;
import org.apache.derby.iapi.sql.dictionary.SchemaDescriptor;

public class SQLSessionContextImpl implements SQLSessionContext {

    private String currentUser;
    private String currentRole;
    private SchemaDescriptor currentDefaultSchema;

    /**
     * Maps a conglomerate id (key) into a Boolean for deferrable constraints.
     * There is a 1-1 correspondence for these backing indexes, they are not
     * shared). If the Boolean value is {@code FALSE}, we have immediate
     * checking, if it is {@code TRUE} we have deferred checking. Cf. SQL
     * SET CONSTRAINT.
     */
    private HashMap<Long, Boolean> constraintModes;

    /**
     * True if all deferrable constraints are deferred in this transaction.
     */
    private Boolean deferredAll;

    public SQLSessionContextImpl (
            SchemaDescriptor sd,
            String currentUser) {
        currentRole = null;
        currentDefaultSchema = sd;
        this.currentUser = currentUser;

        if (constraintModes != null) {
            this.constraintModes = new HashMap<Long,Boolean>(constraintModes);
        }
    }

    public void setRole(String role) {
        currentRole = role;
    }

    public String getRole() {
        return currentRole;
    }

    public void setUser(String user) {
        currentUser = user;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setDefaultSchema(SchemaDescriptor sd) {
        currentDefaultSchema = sd;
    }

    public SchemaDescriptor getDefaultSchema() {
        return currentDefaultSchema;
    }

    /**
     * {@inheritDoc}
     */
    public HashMap<Long, Boolean> getConstraintModes() {
        return constraintModes != null ?
            new HashMap<Long, Boolean>(constraintModes) :
            null;
    }

    /**
     * {@inheritDoc}
     */
    public void setConstraintModes(HashMap<Long, Boolean> hm) {
        this.constraintModes = hm != null ?
                new HashMap<Long, Boolean>(hm) : null;
    }

    /**
     * {@inheritDoc}
     */
    public void setDeferred(long conglomId, boolean deferred) {
        if (constraintModes == null) {
            constraintModes = new HashMap<Long, Boolean>();
        }

        constraintModes.put(Long.valueOf(conglomId),
                                Boolean.valueOf(deferred));
    }

    /**
     * {@inheritDoc}
     */
    public Boolean isDeferred(long conglomId) {
        Boolean v = null;

        if (constraintModes != null) {
            v = constraintModes.get(Long.valueOf(conglomId));
        }

        if (v != null) {
            return v; // Trumps ALL setting since it must have been
                      // set later otherwise it would have been
                      // deleted
        } else {
            return deferredAll;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void resetConstraintModes() {
        if (constraintModes != null) {
            constraintModes.clear();
        }

        deferredAll = null;
    }

    /**
     * {@inheritDoc}
     */
    public void setDeferredAll(Boolean deferred) {
        deferredAll = deferred;
        // This now overrides any individual constraint setting, so
        // clear those.
        if (constraintModes != null) {
            constraintModes.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Boolean getDeferredAll() {
        return deferredAll;
    }
}
