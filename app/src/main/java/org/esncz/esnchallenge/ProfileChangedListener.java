package org.esncz.esnchallenge;

import org.esncz.esnchallenge.model.ProfileData;

/**
 * @author chochy
 * Date: 2019-02-03
 */
public interface ProfileChangedListener {
    void updateProfile(ProfileData data);
}
