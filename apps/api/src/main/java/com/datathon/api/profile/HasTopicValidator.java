package com.datathon.api.profile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class HasTopicValidator implements ConstraintValidator<HasTopic, ProfileRequest> {

    @Override
    public boolean isValid(ProfileRequest profile, ConstraintValidatorContext context) {
        return profile == null
                || !profile.areas().isEmpty()
                || !profile.keywords().isEmpty()
                || !profile.activityCodes().isEmpty();
    }
}
