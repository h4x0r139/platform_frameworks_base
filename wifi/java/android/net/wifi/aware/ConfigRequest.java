/*
 * Copyright (C) 2016 The Android Open Source Project
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

package android.net.wifi.aware;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Defines a request object to configure a Wi-Fi Aware network. Built using
 * {@link ConfigRequest.Builder}. Configuration is requested using
 * {@link WifiAwareManager#attach(AttachCallback, android.os.Handler)}.
 * Note that the actual achieved configuration may be different from the
 * requested configuration - since different applications may request different
 * configurations.
 *
 * @hide
 */
public final class ConfigRequest implements Parcelable {
    /**
     * Lower range of possible cluster ID.
     */
    public static final int CLUSTER_ID_MIN = 0;

    /**
     * Upper range of possible cluster ID.
     */
    public static final int CLUSTER_ID_MAX = 0xFFFF;

    /**
     * Indicates whether 5G band support is requested.
     */
    public final boolean mSupport5gBand;

    /**
     * Specifies the desired master preference.
     */
    public final int mMasterPreference;

    /**
     * Specifies the desired lower range of the cluster ID. Must be lower then
     * {@link ConfigRequest#mClusterHigh}.
     */
    public final int mClusterLow;

    /**
     * Specifies the desired higher range of the cluster ID. Must be higher then
     * {@link ConfigRequest#mClusterLow}.
     */
    public final int mClusterHigh;

    private ConfigRequest(boolean support5gBand, int masterPreference, int clusterLow,
            int clusterHigh) {
        mSupport5gBand = support5gBand;
        mMasterPreference = masterPreference;
        mClusterLow = clusterLow;
        mClusterHigh = clusterHigh;
    }

    @Override
    public String toString() {
        return "ConfigRequest [mSupport5gBand=" + mSupport5gBand + ", mMasterPreference="
                + mMasterPreference + ", mClusterLow=" + mClusterLow + ", mClusterHigh="
                + mClusterHigh + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mSupport5gBand ? 1 : 0);
        dest.writeInt(mMasterPreference);
        dest.writeInt(mClusterLow);
        dest.writeInt(mClusterHigh);
    }

    public static final Creator<ConfigRequest> CREATOR = new Creator<ConfigRequest>() {
        @Override
        public ConfigRequest[] newArray(int size) {
            return new ConfigRequest[size];
        }

        @Override
        public ConfigRequest createFromParcel(Parcel in) {
            boolean support5gBand = in.readInt() != 0;
            int masterPreference = in.readInt();
            int clusterLow = in.readInt();
            int clusterHigh = in.readInt();
            return new ConfigRequest(support5gBand, masterPreference, clusterLow, clusterHigh);
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ConfigRequest)) {
            return false;
        }

        ConfigRequest lhs = (ConfigRequest) o;

        return mSupport5gBand == lhs.mSupport5gBand && mMasterPreference == lhs.mMasterPreference
                && mClusterLow == lhs.mClusterLow && mClusterHigh == lhs.mClusterHigh;
    }

    /**
     * Checks whether the configuration's settings are non-default.
     *
     * @return true if any of the settings are non-default.
     */
    public boolean isNonDefault() {
        return mSupport5gBand || mMasterPreference != 0 || mClusterLow != CLUSTER_ID_MIN
                || mClusterHigh != CLUSTER_ID_MAX;
    }

    @Override
    public int hashCode() {
        int result = 17;

        result = 31 * result + (mSupport5gBand ? 1 : 0);
        result = 31 * result + mMasterPreference;
        result = 31 * result + mClusterLow;
        result = 31 * result + mClusterHigh;

        return result;
    }

    /**
     * Verifies that the contents of the ConfigRequest are valid. Otherwise
     * throws an IllegalArgumentException.
     */
    public void validate() throws IllegalArgumentException {
        if (mMasterPreference < 0) {
            throw new IllegalArgumentException(
                    "Master Preference specification must be non-negative");
        }
        if (mMasterPreference == 1 || mMasterPreference == 255 || mMasterPreference > 255) {
            throw new IllegalArgumentException("Master Preference specification must not "
                    + "exceed 255 or use 1 or 255 (reserved values)");
        }
        if (mClusterLow < CLUSTER_ID_MIN) {
            throw new IllegalArgumentException("Cluster specification must be non-negative");
        }
        if (mClusterLow > CLUSTER_ID_MAX) {
            throw new IllegalArgumentException("Cluster specification must not exceed 0xFFFF");
        }
        if (mClusterHigh < CLUSTER_ID_MIN) {
            throw new IllegalArgumentException("Cluster specification must be non-negative");
        }
        if (mClusterHigh > CLUSTER_ID_MAX) {
            throw new IllegalArgumentException("Cluster specification must not exceed 0xFFFF");
        }
        if (mClusterLow > mClusterHigh) {
            throw new IllegalArgumentException(
                    "Invalid argument combination - must have Cluster Low <= Cluster High");
        }
    }

    /**
     * Builder used to build {@link ConfigRequest} objects.
     */
    public static final class Builder {
        private boolean mSupport5gBand = false;
        private int mMasterPreference = 0;
        private int mClusterLow = CLUSTER_ID_MIN;
        private int mClusterHigh = CLUSTER_ID_MAX;

        /**
         * Specify whether 5G band support is required in this request. Disabled by default.
         *
         * @param support5gBand Support for 5G band is required.
         *
         * @return The builder to facilitate chaining
         *         {@code builder.setXXX(..).setXXX(..)}.
         */
        public Builder setSupport5gBand(boolean support5gBand) {
            mSupport5gBand = support5gBand;
            return this;
        }

        /**
         * Specify the Master Preference requested. The permitted range is 0 (the default) to
         * 255 with 1 and 255 excluded (reserved).
         *
         * @param masterPreference The requested master preference
         *
         * @return The builder to facilitate chaining
         *         {@code builder.setXXX(..).setXXX(..)}.
         */
        public Builder setMasterPreference(int masterPreference) {
            if (masterPreference < 0) {
                throw new IllegalArgumentException(
                        "Master Preference specification must be non-negative");
            }
            if (masterPreference == 1 || masterPreference == 255 || masterPreference > 255) {
                throw new IllegalArgumentException("Master Preference specification must not "
                        + "exceed 255 or use 1 or 255 (reserved values)");
            }

            mMasterPreference = masterPreference;
            return this;
        }

        /**
         * The Cluster ID is generated randomly for new Aware networks. Specify
         * the lower range of the cluster ID. The upper range is specified using
         * the {@link ConfigRequest.Builder#setClusterHigh(int)}. The permitted
         * range is 0 (the default) to the value specified by
         * {@link ConfigRequest.Builder#setClusterHigh(int)}. Equality of Low and High is
         * permitted which restricts the Cluster ID to the specified value.
         *
         * @param clusterLow The lower range of the generated cluster ID.
         *
         * @return The builder to facilitate chaining
         *         {@code builder.setClusterLow(..).setClusterHigh(..)}.
         */
        public Builder setClusterLow(int clusterLow) {
            if (clusterLow < CLUSTER_ID_MIN) {
                throw new IllegalArgumentException("Cluster specification must be non-negative");
            }
            if (clusterLow > CLUSTER_ID_MAX) {
                throw new IllegalArgumentException("Cluster specification must not exceed 0xFFFF");
            }

            mClusterLow = clusterLow;
            return this;
        }

        /**
         * The Cluster ID is generated randomly for new Aware networks. Specify
         * the lower upper of the cluster ID. The lower range is specified using
         * the {@link ConfigRequest.Builder#setClusterLow(int)}. The permitted
         * range is the value specified by
         * {@link ConfigRequest.Builder#setClusterLow(int)} to 0xFFFF (the default). Equality of
         * Low and High is permitted which restricts the Cluster ID to the specified value.
         *
         * @param clusterHigh The upper range of the generated cluster ID.
         *
         * @return The builder to facilitate chaining
         *         {@code builder.setClusterLow(..).setClusterHigh(..)}.
         */
        public Builder setClusterHigh(int clusterHigh) {
            if (clusterHigh < CLUSTER_ID_MIN) {
                throw new IllegalArgumentException("Cluster specification must be non-negative");
            }
            if (clusterHigh > CLUSTER_ID_MAX) {
                throw new IllegalArgumentException("Cluster specification must not exceed 0xFFFF");
            }

            mClusterHigh = clusterHigh;
            return this;
        }

        /**
         * Build {@link ConfigRequest} given the current requests made on the
         * builder.
         */
        public ConfigRequest build() {
            if (mClusterLow > mClusterHigh) {
                throw new IllegalArgumentException(
                        "Invalid argument combination - must have Cluster Low <= Cluster High");
            }

            return new ConfigRequest(mSupport5gBand, mMasterPreference, mClusterLow, mClusterHigh);
        }
    }
}
