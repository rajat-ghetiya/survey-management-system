package com.example.survey.enums;

public enum QuestionType {
    TEXT,               // Free-form text input
    TEXTAREA,           // Multi-line text
    SINGLE_SELECT,      // Radio button - pick one
    MULTI_SELECT,       // Checkboxes - pick multiple
    DROPDOWN,           // Dropdown selection
    RATING,             // Star/number rating (1-5, 1-10)
    OPINION_SCALE,      // Likert scale (Strongly Disagree to Strongly Agree)
    NPS,                // Net Promoter Score (0-10)
    DATE,               // Date picker
    DATE_RANGE,         // Start and end date
    FILE_UPLOAD,        // File attachment
    MATRIX,             // Grid of rows and columns
    RANKING,            // Drag-to-rank options
    ELECTION,           // Single vote, anonymous
    YES_NO,             // Boolean choice
    SLIDER              // Numeric range slider
}
