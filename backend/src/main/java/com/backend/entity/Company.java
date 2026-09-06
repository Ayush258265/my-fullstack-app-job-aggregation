package com.backend.entity;

import java.util.List;

import javax.persistence.* ;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Company extends BaseEntity {
	
	@Column(unique = true, nullable = false, length = 255)
	private String name ;
	
	@Column(length = 255)
	private String website;
	
	 @OneToMany(mappedBy = "companyName", fetch = FetchType.LAZY)
	private List<Job> jobs ;
	
	
	
}
