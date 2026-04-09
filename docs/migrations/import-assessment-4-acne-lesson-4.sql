-- Acne Treatments Lesson 4 assessment bank
-- assessment_id = 4
-- Parsed from docs/assessments/Q & A_Acne Treatments_Lesson No 4.docx. NOTE: Word file has no answer section; is_correct flags use curriculum-inferred keys — verify before production.

START TRANSACTION;

DELETE ao FROM assessment_option ao
INNER JOIN assessment_question aq ON ao.question_id = aq.id
WHERE aq.assessment_id = 4;

DELETE FROM assessment_question WHERE assessment_id = 4;

-- Question 1
INSERT INTO assessment_question (assessment_id, question_text) VALUES (4, 'What is one of the contributing factors to the development of acne?');
SET @q_1 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_1, 'Lack of stress', 0),
(@q_1, 'Excessive hydra on', 0),
(@q_1, 'Genetics', 1),
(@q_1, 'Minimal sun exposure', 0);

-- Question 2
INSERT INTO assessment_question (assessment_id, question_text) VALUES (4, 'How does excess sebum production contribute to acne forma on?');
SET @q_2 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_2, 'It reduces inflammation', 0),
(@q_2, 'It opens up pores', 0),
(@q_2, 'It clogs pores', 1),
(@q_2, 'It decreases bacterial growth', 0);

-- Question 3
INSERT INTO assessment_question (assessment_id, question_text) VALUES (4, 'What distinguishes open comedones (blackheads) from closed comedones (whiteheads)?');
SET @q_3 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_3, 'Blackheads contain pus, while whiteheads do not.', 0),
(@q_3, 'Blackheads have closed pores, while whiteheads have open pores.', 0),
(@q_3, 'Whiteheads have a black appearance, while blackheads appear as small, white bumps.', 0),
(@q_3, 'Blackheads are formed when the pore remains open, while whiteheads have a covered pore.', 1);

-- Question 4
INSERT INTO assessment_question (assessment_id, question_text) VALUES (4, 'Which type of acne lesion is characterized by being the most severe and can cause significant pain and scarring?');
SET @q_4 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_4, 'Papules', 0),
(@q_4, 'Pustules', 0),
(@q_4, 'Nodules', 1),
(@q_4, 'Comedones', 0);

-- Question 5
INSERT INTO assessment_question (assessment_id, question_text) VALUES (4, 'During the assessment phase of consul ng clients on acne treatments, what information should be gathered?');
SET @q_5 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_5, 'Only the client''s current skincare routine', 0),
(@q_5, 'The client''s history of allergies and hormonal factors', 1),
(@q_5, 'The client''s preferred treatment op on', 0),
(@q_5, 'The client''s favourite skincare products', 0);

-- Question 6
INSERT INTO assessment_question (assessment_id, question_text) VALUES (4, 'Which of the following is NOT a treatment op on discussed for acne management?');
SET @q_6 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_6, 'Chemical peels', 0),
(@q_6, 'Topical treatments', 0),
(@q_6, 'Diet plans', 1),
(@q_6, 'Combination therapy', 0);

-- Question 7
INSERT INTO assessment_question (assessment_id, question_text) VALUES (4, 'What should be emphasized when advising clients on proper skincare practices for acne?');
SET @q_7 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_7, 'Encouraging excessive touching or picking at acne lesions', 0),
(@q_7, 'Discouraging the use of sunscreen to protect against UV damage', 0),
(@q_7, 'Recommending gentle cleansers and non-comedogenic moisturizers', 1),
(@q_7, 'Advocating for the use of harsh and abrasive products for exfoliation', 0);

-- Question 8
INSERT INTO assessment_question (assessment_id, question_text) VALUES (4, 'Which chemical solution is known for its ability to penetrate deep into the pores, dissolving oil and dead skin cells, making it effective for treating acne by unclogging pores and reducing inflammation?');
SET @q_8 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_8, 'Glycolic acid', 0),
(@q_8, 'Lac c acid', 0),
(@q_8, 'Salicylic acid', 1),
(@q_8, 'Mandelic acid', 0);

-- Question 9
INSERT INTO assessment_question (assessment_id, question_text) VALUES (4, 'What is the typical treatment recommendation for mild acne when using superficial peels?');
SET @q_9 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_9, 'Performing peels every 4-6 weeks', 0),
(@q_9, 'Using TCA peels at a concentration of 35% or higher', 0),
(@q_9, 'Employing glycolic acid peels to treat acne scars', 0),
(@q_9, 'Performing peels every 2-4 weeks', 1);

-- Question 10
INSERT INTO assessment_question (assessment_id, question_text) VALUES (4, 'What distinguishes deep chemical peels from superficial and medium-depth peels?');
SET @q_10 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_10, 'They contain phenol or TCA beyond 50%.', 1),
(@q_10, 'They are performed every 4-6 weeks.', 0),
(@q_10, 'They primarily target mild acne.', 0),
(@q_10, 'They use glycolic acid or salicylic acid solutions.', 0);

-- Question 11
INSERT INTO assessment_question (assessment_id, question_text) VALUES (4, 'What is a key recommendation for post-treatment care following a chemical peel?');
SET @q_11 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_11, 'Exposing the treated skin to direct sunlight', 0),
(@q_11, 'Using harsh skincare products immediately a er the peel', 0),
(@q_11, 'Moisturizing the skin with a gentle, non-comedogenic moisturizer', 1),
(@q_11, 'Skipping follow-up appointments', 0);

-- Question 12
INSERT INTO assessment_question (assessment_id, question_text) VALUES (4, 'What is the primary objective of high-frequency therapy for acne treatment?');
SET @q_12 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_12, 'Genera ng ozone to reduce inflammation', 0),
(@q_12, 'Producing a thermal effect to soothe the skin', 0),
(@q_12, 'Delivering a low-level electrical current to kill acne-causing bacteria', 1),
(@q_12, 'Increasing blood supply to the skin', 0);

-- Question 13
INSERT INTO assessment_question (assessment_id, question_text) VALUES (4, 'What are the electrodes for high-frequency current typically made of?');
SET @q_13 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_13, 'Plastic', 0),
(@q_13, 'Metal', 0),
(@q_13, 'Glass', 1),
(@q_13, 'Silicon', 0);

-- Question 14
INSERT INTO assessment_question (assessment_id, question_text) VALUES (4, 'What is the purpose of providing protective eyewear to the client before star ng the high frequency acne treatment?');
SET @q_14 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_14, 'To shield the client''s eyes from the bright light emitted by the high frequency device.', 1),
(@q_14, 'To protect the client''s skin from direct exposure to the high frequency current.', 0),
(@q_14, 'To enhance the conductivity of the skin during the treatment.', 0),
(@q_14, 'To minimize the risk of allergic reactions to the conductive gel.', 0);

-- Question 15
INSERT INTO assessment_question (assessment_id, question_text) VALUES (4, 'How should the high frequency device intensity level be adjusted during the acne treatment?');
SET @q_15 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_15, 'Start with a higher intensity and gradually decrease if tolerated.', 0),
(@q_15, 'Set the intensity level to the maximum for better results.', 0),
(@q_15, 'Begin with a lower intensity and gradually increase if tolerated.', 1),
(@q_15, 'Keep the intensity level constant throughout the treatment', 0);

COMMIT;