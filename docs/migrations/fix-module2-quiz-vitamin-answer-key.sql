-- Fix Module 2 quiz (product 1, "Which vitamin is most effective for sun protection...")
-- Legacy seed marked Niacinamide correct; most curricula expect Vitamin C for topical antioxidant/UV-related protection.
-- Run after add-modules-for-locked-flow.sql if learners get 0% while choosing Vitamin C.

UPDATE assessment_option ao
INNER JOIN assessment_question aq ON ao.question_id = aq.id
SET ao.is_correct = CASE ao.option_text
    WHEN 'Vitamin C' THEN 1
    WHEN 'Vitamin E' THEN 0
    WHEN 'Niacinamide' THEN 0
    ELSE ao.is_correct
END
WHERE aq.question_text LIKE '%Which vitamin is most effective for sun protection when applied topically%';
