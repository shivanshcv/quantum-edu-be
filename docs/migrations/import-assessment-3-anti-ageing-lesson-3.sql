-- Anti-Ageing Lesson 3 assessment bank
-- assessment_id = 3
-- Parsed from docs/assessments/Q & A_Anti-Ageing_Lesson No 3.docx (answer key in document).

START TRANSACTION;

DELETE ao FROM assessment_option ao
INNER JOIN assessment_question aq ON ao.question_id = aq.id
WHERE aq.assessment_id = 3;

DELETE FROM assessment_question WHERE assessment_id = 3;

-- Question 1
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'Which of the following procedures is NOT an aesthetic skincare treatment to target specific ageing concerns?');
SET @q_1 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_1, 'Chemical peels', 0),
(@q_1, 'Microdermabrasion', 0),
(@q_1, 'Laser therapy', 0),
(@q_1, 'Hair coloring', 1);

-- Question 2
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'How do aesthetic skincare treatments contribute to promoting long-term skin health and vitality?');
SET @q_2 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_2, 'By accelerating the ageing process', 0),
(@q_2, 'By exclusively focusing on surface-level improvements', 0),
(@q_2, 'By providing comprehensive guidance on skincare regimens', 1),
(@q_2, 'By disregarding the importance of collagen production', 0);

-- Question 3
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'Which factor influences the development of pigmentation issues?');
SET @q_3 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_3, 'Increased collagen and elastin production', 0),
(@q_3, 'Cumulative sun exposure', 1),
(@q_3, 'Enhanced skin hydratio0n', 0),
(@q_3, 'Elevated skin cell turnover', 0);

-- Question 4
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'What contributes to the development of fine lines and wrinkles?');
SET @q_4 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_4, 'Reduced production of collagen and natural oils', 1),
(@q_4, 'Increased skin elasticity and firmness', 0),
(@q_4, 'Enhanced hydra on levels in the skin', 0),
(@q_4, 'Accelerated cell turnover', 0);

-- Question 5
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'Which anti-ageing treatment involves the use of retinoid’s and antioxidants to stimulate collagen production and protect against environmental damage?');
SET @q_5 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_5, 'Chemical peels for anti-ageing', 0),
(@q_5, 'Facial', 0),
(@q_5, 'Topical anti-ageing agents', 1);

-- Question 6
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'Which topical anti-ageing agent is known for its ability to reduce the appearance of fine lines, wrinkles, and sagging skin, while also inhibiting melanin transfer and calming redness and irritation?');
SET @q_6 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_6, 'Retinoid’s', 0),
(@q_6, 'Niacinamide', 1),
(@q_6, 'Hyaluronic acid', 0),
(@q_6, 'Glycolic acid', 0);

-- Question 7
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'Which topical anti-ageing agent functions as a humectant, providing hydration and moisture retention, while also exerting an oxidant and an-inflammatory effects?');
SET @q_7 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_7, 'Niacinamide', 0),
(@q_7, 'Glycolic acid', 0),
(@q_7, 'Hyaluronic acid', 1),
(@q_7, 'Peptides', 0);

-- Question 8
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'Which topical anti-ageing agent functions primarily as an exfoliant’s, removing dead skin cells to reveal a smoother complexion, while also stimulating collagen production and improving skin firmness?');
SET @q_8 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_8, 'Glycolic acid', 1),
(@q_8, 'Peptides', 0),
(@q_8, 'Hyaluronic acid', 0),
(@q_8, 'Retinoids', 0);

-- Question 9
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'What is the first step in the process of consulting and advising clients for anti-ageing services involving the application of topical agents?');
SET @q_9 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_9, 'Provide a after care instruction', 0),
(@q_9, 'Demonstrate application techniques', 0),
(@q_9, 'Assess and conduct a thorough consultation', 1),
(@q_9, 'Tailor a personalized treatment plan', 0);

-- Question 10
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'Which type of chemical peel uses mild acids like alpha hydroxy acids (AHAs) or beta hydroxy acids (BHAs) to gently exfoliate the outermost layer of the skin?');
SET @q_10 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_10, 'Medium Peels', 0),
(@q_10, 'Deep Peels', 0),
(@q_10, 'Superficial Peels', 1),
(@q_10, 'Intensive Peels', 0);

-- Question 11
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'What is the purpose of neutralization in the removal process of stronger chemical peels?');
SET @q_11 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_11, 'To cleanse the skin', 0),
(@q_11, 'To hydrate the skin', 0),
(@q_11, 'To stop the exfoliating ac on of the peel', 1),
(@q_11, 'To stimulate collagen production', 0);

-- Question 12
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'What is a recommended component of post-peel care after a chemical peel?');
SET @q_12 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_12, 'Increasing sun exposure', 0),
(@q_12, 'Using harsh skincare products', 0),
(@q_12, 'Applying soothing products like moisturizers or hydrating serums', 1),
(@q_12, 'Avoiding follow-up appointments', 0);

-- Question 13
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'Which component of facials is essential for improving skin texture, smoothness, and promo ng cell turnover?');
SET @q_13 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_13, 'Hydra on and nourishment', 0),
(@q_13, 'Specialized anti-ageing treatments', 0),
(@q_13, 'Exfoliation', 1),
(@q_13, 'Deep cleansing', 0);

-- Question 14
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'What is a significant benefit of regular facials as part of an anti-ageing skincare regimen?');
SET @q_14 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_14, 'Enhancing relaxation and stress relief', 0),
(@q_14, 'Promoting overall well-being', 0),
(@q_14, 'Achieving and maintaining a youthful, radiant complexion', 1),
(@q_14, 'Providing hydration and nourishment to the skin', 0);

-- Question 15
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'What is a recommended step in performing a hydra ng facial to prepare the skin for be er product absorption?');
SET @q_15 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_15, 'Skip exfoliation to avoid overstimulation of the skin', 0),
(@q_15, 'Apply steam to open up pores', 1),
(@q_15, 'Use a harsh exfoliant to deeply cleanse the skin', 0),
(@q_15, 'Skip moisturizer to allow hydrating ingredients to penetrate deeply', 0);

-- Question 16
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'Which component is typically included in hydrating masks used during hydrating facials?');
SET @q_16 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_16, 'Salicylic acid and benzoyl peroxide', 0),
(@q_16, 'Astringents like witch hazel', 0),
(@q_16, 'Hyaluronic acid, glycerin, and natural oils', 1),
(@q_16, 'Alcohol-based ingredients that can dry out the skin', 0);

-- Question 17
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'What is a primary goal of performing an anti-wrinkle facial?');
SET @q_17 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_17, 'Removing makeup and impurities from the skin', 0),
(@q_17, 'Providing relaxation through massage techniques', 0),
(@q_17, 'Targeting signs of ageing such as fine lines and wrinkles', 1),
(@q_17, 'Hydrating and moisturizing the skin', 0);

-- Question 18
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'What is a recommended product to apply during the massage step of an an-wrinkle facial to provide lubrication and anti-ageing benefits?');
SET @q_18 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_18, 'Astringent toner', 0),
(@q_18, 'Hydrating serum or facial oil', 1),
(@q_18, 'Sun screen', 0),
(@q_18, 'Makeup primer', 0);

-- Question 19
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'What is the primary purpose of using a brightening serum in a brightening facial?');
SET @q_19 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_19, 'Exfoliating dead skin cells', 0),
(@q_19, 'Providing hydra on to the skin', 0),
(@q_19, 'Lightening dark spots and evening out skin tone', 1),
(@q_19, 'Preparing the skin for further treatment', 0);

-- Question 20
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'Why is it essential to apply sun screen after a brightening facial?');
SET @q_20 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_20, 'To remove excess product from the skin', 0),
(@q_20, 'To provide hydra on to the skin', 0),
(@q_20, 'To protect the skin from UV damage, which can worsen hyper pigmentation', 1),
(@q_20, 'To soothe and calm the skin after treatment', 0);

-- Question 21
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'What is the first step in preparing for a micro current facial procedure?');
SET @q_21 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_21, 'Applying a thick layer of moisturizer', 0),
(@q_21, 'Cleansing the client''s face to remove makeup and impurities', 1),
(@q_21, 'Selecting the appropriate micro current settings', 0),
(@q_21, 'Applying a conductive gel to the client''s face', 0);

-- Question 22
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'How are micro current probes or wands typically used during the facial procedure?');
SET @q_22 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_22, 'They are used to apply exfoliating products to the skin', 0),
(@q_22, 'They are used to extract impurities from the pores', 0),
(@q_22, 'They are gently glided over the skin in upward and outward motions to target specific facial muscles', 1),
(@q_22, 'They are used to apply sun screen to the skin', 0);

-- Question 23
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'Why is scalp analysis conducted before the hair massage treatment?');
SET @q_23 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_23, 'To select appropriate hair care products', 0),
(@q_23, 'To perform the massage technique', 0),
(@q_23, 'To assess the client''s hair concerns', 1),
(@q_23, 'To provide stress relief', 0);

-- Question 24
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'What is the purpose of conducting a deep conditioning treatment in the salon for dry, bri le, and frizzy hair?');
SET @q_24 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_24, 'To assess the client''s hair condition', 0),
(@q_24, 'To analyze the causes of hair dryness', 0),
(@q_24, 'To provide intense hydra on, nourishment, and repair', 1),
(@q_24, 'To recommend suitable home care products', 0);

-- Question 25
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'Why is steam or heat therapy utilized during the deep conditioning treatment?');
SET @q_25 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_25, 'To minimize damage and breakage', 0),
(@q_25, 'To assess the effectiveness of the treatment', 0),
(@q_25, 'To open the hair cuticle and enhance absorption of the conditioner', 1),
(@q_25, 'To provide gentle styling techniques', 0);

-- Question 26
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'Which aspect of hair spa treatments focuses on repairing damaged or brittle hair?');
SET @q_26 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_26, 'Scalp rejuvenation', 0),
(@q_26, 'Hydra on and nourishment', 0),
(@q_26, 'Hair repair and strengthening', 1),
(@q_26, 'Relaxation and stress relief', 0);

-- Question 27
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'What is a key benefit of scalp rejuvenation during a hair spa treatment?');
SET @q_27 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_27, 'Promoting hydration and nourishment', 0),
(@q_27, 'Repairing and strengthening the hair shaft', 0),
(@q_27, 'Stimulating blood circulation and relaxation', 1),
(@q_27, 'Improving hair texture and appearance', 0);

-- Question 28
INSERT INTO assessment_question (assessment_id, question_text) VALUES (3, 'How do hair spa treatments primarily contribute to stress relief?');
SET @q_28 = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, is_correct) VALUES
(@q_28, 'By repairing damaged hair', 0),
(@q_28, 'Through deep cleansing of the scalp', 0),
(@q_28, 'By providing a luxurious and indulgent experience', 1),
(@q_28, 'By promoting hydration and nourishment', 0);

COMMIT;