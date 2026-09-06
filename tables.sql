-- Create Database
CREATE DATABASE IF NOT EXISTS jobfinder_db;
USE jobfinder_db;

select * from users ;

SET SQL_SAFE_UPDATES = 1;
-- Update all users with NULL daily_interviews_count to 0
UPDATE users SET daily_interviews_count = 0 WHERE daily_interviews_count IS NULL;

-- Update all users with NULL last_interview_date (optional)
UPDATE users SET last_interview_date = NULL WHERE last_interview_date IS NULL;

-- Verify the fix
SELECT id, email, daily_interviews_count, last_interview_date FROM users;

update users set role = 'Admin' where email = 'admin@jobfinder.com' ;

DELETE FROM users WHERE email = 'admin@jobfinder.com';

UPDATE users 
SET password = '$2a$10$0UlaxCCkkLLdWOap4o6n.euGlSVDQKg8pFpgkJNGluVStQMvcl1H.' 
WHERE email = 'ayush1406pal@gmail.com';

SELECT DISTINCT experience_required FROM jobs WHERE is_active = 1 ORDER BY experience_required;

-- Step 1: Disable safe update mode
SET SQL_SAFE_UPDATES = 0;

-- Step 2: Remove garbage values (60 years, 75 years, etc.)
UPDATE jobs SET experience_required = NULL 
WHERE experience_required LIKE '%60 years%' 
   OR experience_required LIKE '%75 years%' 
   OR experience_required LIKE '%30 years%'
   OR experience_required LIKE '%18 years%' 
   OR experience_required LIKE '%40 years%'
   OR experience_required LIKE '%20 years%'
   OR experience_required LIKE '%25 years%';

-- Step 3: Normalize to 'Fresher'
UPDATE jobs SET experience_required = 'Fresher' 
WHERE experience_required LIKE '%0-1%' 
   OR experience_required LIKE '%Fresher%' 
   OR experience_required LIKE '%Entry%' 
   OR experience_required LIKE '%entry%'
   OR experience_required LIKE '%0 years%' 
   OR experience_required LIKE '%1 year%';

-- Step 4: Normalize to '1-4 years'
UPDATE jobs SET experience_required = '1-4 years' 
WHERE experience_required LIKE '%1-2%' 
   OR experience_required LIKE '%1-3%' 
   OR experience_required LIKE '%1-4%' 
   OR experience_required LIKE '%1 year%' 
   OR experience_required LIKE '%2 years%' 
   OR experience_required LIKE '%2+%'
   OR experience_required LIKE '%3 years%' 
   OR experience_required LIKE '%3+%'
   OR experience_required LIKE '%2-3%';

-- Step 5: Normalize to '4-10 years'
UPDATE jobs SET experience_required = '4-10 years' 
WHERE experience_required LIKE '%3-5%' 
   OR experience_required LIKE '%4-6%' 
   OR experience_required LIKE '%4-10%' 
   OR experience_required LIKE '%5-10%' 
   OR experience_required LIKE '%4 years%' 
   OR experience_required LIKE '%5 years%'
   OR experience_required LIKE '%6 years%' 
   OR experience_required LIKE '%8 years%'
   OR experience_required LIKE '%8+%' 
   OR experience_required LIKE '%7-10%'
   OR experience_required LIKE '%5-7%' 
   OR experience_required LIKE '%4-5%'
   OR experience_required LIKE '%3-7%';

-- Step 6: Normalize to '10+ years'
UPDATE jobs SET experience_required = '10+ years' 
WHERE experience_required LIKE '%10+%' 
   OR experience_required LIKE '%12+%' 
   OR experience_required LIKE '%15+%' 
   OR experience_required LIKE '%10 years%'
   OR experience_required LIKE '%10-15%' 
   OR experience_required LIKE '%10-12%'
   OR experience_required LIKE '%15 years%';

-- Step 7: View cleaned data
SELECT experience_required, COUNT(*) as count 
FROM jobs 
WHERE experience_required IS NOT NULL 
GROUP BY experience_required 
ORDER BY count DESC;

-- Step 8: Re-enable safe update mode
SET SQL_SAFE_UPDATES = 1;

-- 1. Users Table (with profile fields)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    role ENUM('USER', 'PREMIUM', 'ADMIN') DEFAULT 'USER',
    free_interviews_used INT DEFAULT 0,
    years_of_experience INT DEFAULT 0,
    primary_skills TEXT,
    preferred_location VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role)
);

-- 2. Companies Table
CREATE TABLE companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    website VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_name (name)
);

-- 3. Jobs Table
CREATE TABLE jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    experience_required VARCHAR(100),
    skills TEXT,
    description TEXT,
    source VARCHAR(50),
    source_job_id VARCHAR(255),
    apply_url VARCHAR(500),
    posted_date DATE,
    dedupe_hash VARCHAR(64) UNIQUE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_company (company_name),
    INDEX idx_location (location),
    INDEX idx_posted_date (posted_date),
    INDEX idx_dedupe_hash (dedupe_hash),
    INDEX idx_active (is_active),
    FULLTEXT INDEX idx_skills (skills),
    FULLTEXT INDEX idx_title_desc (title, description)
);

-- 4. Apply Clicks Table (Self-Reported Tracking)
CREATE TABLE apply_clicks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    applied BOOLEAN DEFAULT FALSE,
    clicked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP DEFAULT (CURRENT_TIMESTAMP + INTERVAL 30 DAY),
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    INDEX idx_user_job (user_id, job_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_applied (applied)
);

-- 5. Interview Sessions (Metadata Only - No Q&A Storage)
CREATE TABLE interview_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    topic VARCHAR(255) NOT NULL,
    duration_minutes INT NOT NULL,
    total_questions INT NOT NULL,
    answered_questions INT DEFAULT 0,
    completed BOOLEAN DEFAULT FALSE,
    overall_score DECIMAL(5,2),
    common_mistakes TEXT,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_completed (user_id, completed),
    INDEX idx_started_at (started_at)
);

-- 6. Audit Log (Optional - For Tracking Changes)
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100),
    entity_type VARCHAR(50),
    entity_id BIGINT,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_action (user_id, action),
    INDEX idx_created_at (created_at)
);

-- Sample Data
INSERT INTO users (email, password, first_name, last_name, role, primary_skills, years_of_experience) 
VALUES ('admin@jobfinder.com', '$2a$10$YourEncodedPassword', 'Admin', 'User', 'ADMIN', 'Java,Spring,React', 10);

-- Step 1: Disable safe update mode
SET SQL_SAFE_UPDATES = 0;

-- Step 2: Normalize to country names
UPDATE jobs SET location = 'USA' 
WHERE location LIKE '%USA%' OR location LIKE '%United States%' 
OR location LIKE '%US%' OR location LIKE '%America%';

UPDATE jobs SET location = 'Canada' 
WHERE location LIKE '%Canada%' OR location LIKE '%Canadian%';

UPDATE jobs SET location = 'UK' 
WHERE location LIKE '%UK%' OR location LIKE '%United Kingdom%' 
OR location LIKE '%England%' OR location LIKE '%London%';

UPDATE jobs SET location = 'Germany' 
WHERE location LIKE '%Germany%' OR location LIKE '%German%';

UPDATE jobs SET location = 'France' 
WHERE location LIKE '%France%' OR location LIKE '%French%';

UPDATE jobs SET location = 'Australia' 
WHERE location LIKE '%Australia%' OR location LIKE '%Australian%';

UPDATE jobs SET location = 'Singapore' 
WHERE location LIKE '%Singapore%';

UPDATE jobs SET location = 'India' 
WHERE location LIKE '%India%' OR location LIKE '%Indian%' 
OR location LIKE '%Bangalore%' OR location LIKE '%Mumbai%' 
OR location LIKE '%Delhi%' OR location LIKE '%Hyderabad%'
OR location LIKE '%Pune%' OR location LIKE '%Chennai%'
OR location LIKE '%Gurugram%';

UPDATE jobs SET location = 'Remote' 
WHERE location LIKE '%Remote%' OR location LIKE '%Anywhere%' 
OR location LIKE '%Worldwide%' OR location LIKE '%Global%';

UPDATE jobs SET location = 'Europe' 
WHERE location LIKE '%Europe%' AND location NOT LIKE '%USA%' 
AND location NOT LIKE '%UK%' AND location NOT LIKE '%Germany%';

UPDATE jobs SET location = 'Asia' 
WHERE location LIKE '%Asia%' OR location LIKE '%APAC%';

UPDATE jobs SET location = 'Africa' 
WHERE location LIKE '%Africa%';

UPDATE jobs SET location = 'South America' 
WHERE location LIKE '%South America%' OR location LIKE '%LATAM%';

-- Step 3: Set any remaining NULL/empty to 'Remote'
UPDATE jobs SET location = 'Remote' 
WHERE location IS NULL OR location = '';

-- Step 4: View cleaned location data
SELECT location, COUNT(*) as count 
FROM jobs 
GROUP BY location 
ORDER BY count DESC;

-- Step 5: Re-enable safe update mode
SET SQL_SAFE_UPDATES = 1;

SELECT 
    location, 
    experience_required, 
    COUNT(*) as count 
FROM jobs 
WHERE is_active = 1 
GROUP BY location, experience_required 
ORDER BY location, experience_required;


SET SQL_SAFE_UPDATES = 0;

-- Clean locations - extract country names
UPDATE jobs 
SET location = 'USA' 
WHERE location LIKE '%USA%' 
   OR location LIKE '%United States%'
   OR location LIKE '%New York%'
   OR location LIKE '%San Francisco%'
   OR location LIKE '%Chicago%'
   OR location LIKE '%Texas%'
   OR location LIKE '%California%'
   OR location LIKE '%NV%';

UPDATE jobs 
SET location = 'India' 
WHERE location LIKE '%India%' 
   OR location LIKE '%Ahmedabad%' 
   OR location LIKE '%Bhubaneshwar%'
   OR location LIKE '%Bijnor%'
   OR location LIKE '%Chittoor%'
   OR location LIKE '%Coimbatore%'
   OR location LIKE '%Guwahati%'
   OR location LIKE '%Indore%'
   OR location LIKE '%Nagpur%'
   OR location LIKE '%Nellore%'
   OR location LIKE '%Siliguri%'
   OR location LIKE '%Thiruvananthapuram%'
   OR location LIKE '%Vijayawada%';

UPDATE jobs 
SET location = 'UK' 
WHERE location LIKE '%UK%' 
   OR location LIKE '%United Kingdom%'
   OR location LIKE '%London%'
   OR location LIKE '%Glasgow%'
   OR location LIKE '%Newcastle%'
   OR location LIKE '%Belfast%';

UPDATE jobs 
SET location = 'Canada' 
WHERE location LIKE '%Canada%' 
   OR location LIKE '%Toronto%'
   OR location LIKE '%Vancouver%'
   OR location LIKE '%Edmonton%'
   OR location LIKE '%Winnipeg%'
   OR location LIKE '%Regina%'
   OR location LIKE '%Saskatoon%'
   OR location LIKE '%Kitchener%';

UPDATE jobs 
SET location = 'Australia' 
WHERE location LIKE '%Australia%' 
   OR location LIKE '%Sydney%'
   OR location LIKE '%Melbourne%'
   OR location LIKE '%Brisbane%'
   OR location LIKE '%Perth%'
   OR location LIKE '%Adelaide%'
   OR location LIKE '%Canberra%'
   OR location LIKE '%Darwin%'
   OR location LIKE '%Hobart%'
   OR location LIKE '%Geelong%'
   OR location LIKE '%Alice Springs%';

UPDATE jobs 
SET location = 'Europe' 
WHERE location LIKE '%Europe%' 
   OR location LIKE '%Germany%'
   OR location LIKE '%France%'
   OR location LIKE '%Spain%'
   OR location LIKE '%Portugal%'
   OR location LIKE '%Sweden%'
   OR location LIKE '%Norway%'
   OR location LIKE '%Italy%';

UPDATE jobs 
SET location = 'Remote' 
WHERE location LIKE '%Remote%' 
   OR location LIKE '%Anywhere%'
   OR location LIKE '%Worldwide%'
   OR location LIKE '%Global%'
   OR location LIKE '%LATAM%';

-- Set remaining unknown locations to 'Remote'
UPDATE jobs 
SET location = 'Remote' 
WHERE location IS NULL 
   OR location = '' 
   OR location NOT IN ('USA', 'India', 'UK', 'Canada', 'Australia', 'Europe', 'Remote', 'Asia', 'Africa', 'South America');

SET SQL_SAFE_UPDATES = 1;


SET SQL_SAFE_UPDATES = 0;

-- Remove garbage values
UPDATE jobs 
SET experience_required = NULL 
WHERE experience_required LIKE '%40 years%' 
   OR experience_required LIKE '%10 years%'
   OR experience_required LIKE '%5 years%'
   OR experience_required LIKE '%1 year%'
   OR experience_required LIKE '%2 years%'
   OR experience_required LIKE '%3 years%'
   OR experience_required LIKE '%4 years%'
   OR experience_required LIKE '%6 years%'
   OR experience_required LIKE '%8 years%'
   OR experience_required LIKE '%12 years%'
   OR experience_required LIKE '%15 years%'
   OR experience_required LIKE '%1 year%'
   AND experience_required NOT LIKE '%+%'
   AND experience_required NOT LIKE '%-%';

-- Normalize to 'Fresher'
UPDATE jobs 
SET experience_required = 'Fresher' 
WHERE experience_required LIKE '%Fresher%' 
   OR experience_required LIKE '%Entry%' 
   OR experience_required LIKE '%0-1%'
   OR experience_required LIKE '%0 years%';

-- Normalize to '1-4 years'
UPDATE jobs 
SET experience_required = '1-4 years' 
WHERE experience_required LIKE '%1-2%' 
   OR experience_required LIKE '%1-3%' 
   OR experience_required LIKE '%1-4%' 
   OR experience_required LIKE '%1 year%' 
   OR experience_required LIKE '%2 years%' 
   OR experience_required LIKE '%2+%'
   OR experience_required LIKE '%3 years%' 
   OR experience_required LIKE '%3+%'
   OR experience_required LIKE '%2-3%';

-- Normalize to '4-10 years'
UPDATE jobs 
SET experience_required = '4-10 years' 
WHERE experience_required LIKE '%3-5%' 
   OR experience_required LIKE '%4-6%' 
   OR experience_required LIKE '%4-10%' 
   OR experience_required LIKE '%5-10%' 
   OR experience_required LIKE '%4 years%' 
   OR experience_required LIKE '%5 years%'
   OR experience_required LIKE '%6 years%' 
   OR experience_required LIKE '%8 years%'
   OR experience_required LIKE '%8+%' 
   OR experience_required LIKE '%7-10%'
   OR experience_required LIKE '%5-7%' 
   OR experience_required LIKE '%4-5%'
   OR experience_required LIKE '%3-7%'
   OR experience_required LIKE '%3 years%';

-- Normalize to '10+ years'
UPDATE jobs 
SET experience_required = '10+ years' 
WHERE experience_required LIKE '%10+%' 
   OR experience_required LIKE '%12+%' 
   OR experience_required LIKE '%15+%' 
   OR experience_required LIKE '%10 years%'
   OR experience_required LIKE '%10-15%' 
   OR experience_required LIKE '%10-12%'
   OR experience_required LIKE '%15 years%';

-- Set remaining NULL to 'Fresher'
UPDATE jobs 
SET experience_required = 'Fresher' 
WHERE experience_required IS NULL;

SET SQL_SAFE_UPDATES = 1;

-- Check cleaned locations
SELECT location, COUNT(*) FROM jobs 
WHERE is_active = 1 
GROUP BY location 
ORDER BY location;

UPDATE users SET role = 'ADMIN' WHERE email = 'postman@test.com';

-- Check cleaned experience
SELECT experience_required, COUNT(*) FROM jobs 
WHERE is_active = 1 
GROUP BY experience_required 
ORDER BY experience_required;

DELETE FROM users WHERE email = 'test@example.com';
SELECT id, email, role FROM users WHERE email = 'postman@test.com';

SELECT location, experience_required, COUNT(*) 
FROM jobs 
WHERE is_active = 1 
GROUP BY location, experience_required 
ORDER BY location, experience_required;
