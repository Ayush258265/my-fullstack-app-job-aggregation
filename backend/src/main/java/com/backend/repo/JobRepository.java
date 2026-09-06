package com.backend.repo;

import com.backend.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    @Query("SELECT j FROM Job j WHERE " +
            "(:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:company IS NULL OR LOWER(j.companyName) LIKE LOWER(CONCAT('%', :company, '%'))) AND " +
            "(:experience IS NULL OR LOWER(j.experienceRequired) LIKE LOWER(CONCAT('%', :experience, '%'))) AND " +
            "(:skills IS NULL OR LOWER(j.skills) LIKE LOWER(CONCAT('%', :skills, '%'))) AND " +
            "(:fromDate IS NULL OR j.postedDate >= :fromDate) AND " +
            "j.isActive = true")
    Page<Job> searchJobs(@Param("title") String title,
            @Param("location") String location,
            @Param("company") String company,
            @Param("experience") String experience,
            @Param("skills") String skills,
            @Param("fromDate") LocalDate fromDate,
            Pageable pageable);

    @Query("SELECT DISTINCT j.location FROM Job j WHERE j.isActive = true AND j.location IS NOT NULL")
    List<String> findAllLocations();

    @Query("SELECT DISTINCT j.companyName FROM Job j WHERE j.isActive = true AND j.companyName IS NOT NULL")
    List<String> findAllCompanies();

    @Query("SELECT DISTINCT j.experienceRequired FROM Job j WHERE j.isActive = true AND j.experienceRequired IS NOT NULL")
    List<String> findAllExperienceLevels();

    boolean existsByDedupeHash(String dedupeHash);

    // ===== ✅ ADDED: New queries for matching =====
    @Query("SELECT DISTINCT j.skills FROM Job j WHERE j.isActive = true AND j.skills IS NOT NULL")
    List<String> findAllJobSkills();

    @Query("SELECT DISTINCT j.experienceRequired FROM Job j WHERE j.isActive = true AND j.experienceRequired IS NOT NULL AND j.experienceRequired LIKE %:keyword%")
    List<String> findExperienceLevelsContaining(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT j.location FROM Job j WHERE j.isActive = true AND j.location IS NOT NULL AND LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<String> findLocationsContaining(@Param("location") String location);

    // ===== ✅ END ADDED =====
}