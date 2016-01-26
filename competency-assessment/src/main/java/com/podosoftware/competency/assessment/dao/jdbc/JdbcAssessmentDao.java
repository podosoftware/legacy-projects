package com.podosoftware.competency.assessment.dao.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;

import com.podosoftware.competency.assessment.Assessment;
import com.podosoftware.competency.assessment.Assessment.State;
import com.podosoftware.competency.assessment.AssessmentScheme;
import com.podosoftware.competency.assessment.DefaultAssessment;
import com.podosoftware.competency.assessment.DefaultAssessmentScheme;
import com.podosoftware.competency.assessment.DefaultRatingLevel;
import com.podosoftware.competency.assessment.DefaultRatingScheme;
import com.podosoftware.competency.assessment.JobSelection;
import com.podosoftware.competency.assessment.RatingLevel;
import com.podosoftware.competency.assessment.RatingScheme;
import com.podosoftware.competency.assessment.Subject;
import com.podosoftware.competency.assessment.dao.AssessmentDao;

import architecture.ee.jdbc.property.dao.ExtendedPropertyDao;
import architecture.ee.spring.jdbc.support.ExtendedJdbcDaoSupport;

public class JdbcAssessmentDao extends ExtendedJdbcDaoSupport implements AssessmentDao  {

	private String assessmentSchemeSequencerName = "ASSESSMENT_SCHEME";
	private String assessmentSchemePropertyTableName = "CA_ASSESSMENT_PLAN_PROPERTY";
	private String assessmentSchemePropertyPrimaryColumnName = "ASSESSMENT_SCHEME_ID";

	private String assessmentSequencerName = "ASSESSMENT";
	private String assessmentPropertyTableName = "CA_ASSESSMENT_SCHEME_PROPERTY";
	private String assessmentPropertyPrimaryColumnName = "ASSESSMENT_ID";	
	
	
	private String ratingSchemeSequencerName = "RATING_SCHEME";
	private String ratingLevelSequencerName = "RATING_LEVEL";		
	private String ratingSchemePropertyTableName = "CA_RATING_SCHEME_PROPERTY";
	private String ratingSchemePropertyPrimaryColumnName = "RATING_SCHEME_ID";
	
	private String assessmentSubjectSequencerName = "ASSESSMENT_SUBJECT";
	
	private ExtendedPropertyDao extendedPropertyDao;
	
	private final RowMapper<Assessment> assessmentMapper = new RowMapper<Assessment>(){		
		public Assessment mapRow(ResultSet rs, int rowNum) throws SQLException {				
			DefaultAssessment scheme = new DefaultAssessment();
			scheme.setAssessmentId(rs.getLong("ASSESSMENT_ID"));
			scheme.setObjectType(rs.getInt("OBJECT_TYPE"));
			scheme.setObjectId(rs.getLong("OBJECT_ID"));
			scheme.setName(rs.getString("NAME"));
			scheme.setDescription(rs.getString("DESCRIPTION"));
			scheme.setState(State.valueOf(rs.getString("STATE").toUpperCase()));
			scheme.setRatingScheme(new DefaultRatingScheme(rs.getLong("RATING_SCHEME_ID")));
			scheme.setMultipleApplyAllowed(rs.getInt("MULTIPLE_APPLY_ALLOWED") == 1 ? true : false );
			scheme.setFeedbackEnabled(rs.getInt("FEEDBACK_ENABLED") == 1 ? true : false );
			scheme.setCreationDate( rs.getDate("CREATION_DATE") ); 
			scheme.setModifiedDate( rs.getDate("MODIFIED_DATE") );
			scheme.setStartDate( rs.getDate("START_DATE") ); 
			scheme.setEndDate( rs.getDate("END_DATE") );
			return scheme;
		}		
	};
	
	
	private final RowMapper<AssessmentScheme> assessmentSchemeMapper = new RowMapper<AssessmentScheme>(){		
		public AssessmentScheme mapRow(ResultSet rs, int rowNum) throws SQLException {				
			DefaultAssessmentScheme scheme = new DefaultAssessmentScheme();
			scheme.setAssessmentSchemeId(rs.getLong("ASSESSMENT_SCHEME_ID"));
			scheme.setObjectType(rs.getInt("OBJECT_TYPE"));
			scheme.setObjectId(rs.getLong("OBJECT_ID"));
			scheme.setName(rs.getString("NAME"));
			scheme.setDescription(rs.getString("DESCRIPTION"));
			scheme.setRatingScheme(new DefaultRatingScheme(rs.getLong("RATING_SCHEME_ID")));
			scheme.setMultipleApplyAllowed(rs.getInt("MULTIPLE_APPLY_ALLOWED") == 1 ? true : false );
			scheme.setFeedbackEnabled(rs.getInt("FEEDBACK_ENABLED") == 1 ? true : false );
			scheme.setCreationDate( rs.getDate("CREATION_DATE") ); 
			scheme.setModifiedDate( rs.getDate("MODIFIED_DATE") );
			return scheme;
		}		
	};
	
	
	private final RowMapper<RatingScheme> ratingSchemeMapper = new RowMapper<RatingScheme>(){		
		public RatingScheme mapRow(ResultSet rs, int rowNum) throws SQLException {	
			DefaultRatingScheme scheme = new DefaultRatingScheme();
			scheme.setRatingSchemeId(rs.getLong("RATING_SCHEME_ID"));
			scheme.setObjectType(rs.getInt("OBJECT_TYPE"));
			scheme.setObjectId(rs.getLong("OBJECT_ID"));
			scheme.setName(rs.getString("NAME"));
			scheme.setDescription(rs.getString("DESCRIPTION"));
			scheme.setScale(rs.getInt("SCALE"));
			scheme.setCreationDate( rs.getDate("CREATION_DATE") ); 
			scheme.setModifiedDate( rs.getDate("MODIFIED_DATE") ); 		
			return scheme;
		}		
	};

	private final RowMapper<RatingLevel> ratingLevelMapper = new RowMapper<RatingLevel>(){		
		public RatingLevel mapRow(ResultSet rs, int rowNum) throws SQLException {	
			DefaultRatingLevel level = new DefaultRatingLevel();
			level.setRatingSchemeId(rs.getLong("RATING_SCHEME_ID"));			
			level.setRatingLevelId(rs.getLong("RATING_LEVEL_ID"));
			level.setTitle(rs.getString("TITLE"));
			level.setScore(rs.getInt("SCORE"));	
			return level;
		}		
	};

	
	private final RowMapper<JobSelection> jobSelectionMapper = new RowMapper<JobSelection>(){		
		public JobSelection mapRow(ResultSet rs, int rowNum) throws SQLException {	
			JobSelection jobSelection = new JobSelection();
			jobSelection.setSelectionId(rs.getLong("SELECTION_ID"));
			jobSelection.setObjectType(rs.getInt("OBJECT_TYPE"));
			jobSelection.setObjectId(rs.getLong("OBJECT_ID"));
			jobSelection.setClassifyType(rs.getLong("CLASSIFY_TYPE"));
			jobSelection.setClassifiedMajorityId(rs.getLong("L_CLASSIFIED_ID"));
			jobSelection.setClassifiedMiddleId(rs.getLong("M_CLASSIFIED_ID"));
			jobSelection.setClassifiedMinorityId(rs.getLong("S_CLASSIFIED_ID"));
			jobSelection.setJobId( rs.getLong("JOB_ID") ); 
			return jobSelection;
		}		
	};

	private final RowMapper<Subject> subjectMapper = new RowMapper<Subject>(){		
		public Subject mapRow(ResultSet rs, int rowNum) throws SQLException {	
			Subject jobSelection = new Subject();			
			jobSelection.setSubjectId(rs.getLong("SUBJECT_ID"));
			jobSelection.setObjectType(rs.getInt("OBJECT_TYPE"));
			jobSelection.setObjectId(rs.getLong("OBJECT_ID"));
			jobSelection.setSubjectObjectType(rs.getInt("SUBJECT_OBJECT_TYPE"));
			jobSelection.setSubjectObjectId(rs.getLong("SUBJECT_OBJECT_ID"));
			return jobSelection;
		}		
	};
	
	
	public JdbcAssessmentDao() {
	}

	public String getRatingSchemeSequencerName() {
		return ratingSchemeSequencerName;
	}

	public void setRatingSchemeSequencerName(String ratingSchemeSequencerName) {
		this.ratingSchemeSequencerName = ratingSchemeSequencerName;
	}

	public String getRatingSchemePropertyTableName() {
		return ratingSchemePropertyTableName;
	}

	public void setRatingSchemePropertyTableName(String ratingSchemePropertyTableName) {
		this.ratingSchemePropertyTableName = ratingSchemePropertyTableName;
	}

	public String getRatingSchemePropertyPrimaryColumnName() {
		return ratingSchemePropertyPrimaryColumnName;
	}

	public void setRatingSchemePropertyPrimaryColumnName(String ratingSchemePropertyPrimaryColumnName) {
		this.ratingSchemePropertyPrimaryColumnName = ratingSchemePropertyPrimaryColumnName;
	}
	
	
	

	public String getAssessmentSchemeSequencerName() {
		return assessmentSchemeSequencerName;
	}

	public void setAssessmentSchemeSequencerName(String assessmentSchemeSequencerName) {
		this.assessmentSchemeSequencerName = assessmentSchemeSequencerName;
	}

	public String getAssessmentSchemePropertyTableName() {
		return assessmentSchemePropertyTableName;
	}

	public void setAssessmentSchemePropertyTableName(String assessmentSchemePropertyTableName) {
		this.assessmentSchemePropertyTableName = assessmentSchemePropertyTableName;
	}

	public String getAssessmentSchemePropertyPrimaryColumnName() {
		return assessmentSchemePropertyPrimaryColumnName;
	}

	public void setAssessmentSchemePropertyPrimaryColumnName(String assessmentSchemePropertyPrimaryColumnName) {
		this.assessmentSchemePropertyPrimaryColumnName = assessmentSchemePropertyPrimaryColumnName;
	}

	public ExtendedPropertyDao getExtendedPropertyDao() {
		return extendedPropertyDao;
	}

	public void setExtendedPropertyDao(ExtendedPropertyDao extendedPropertyDao) {
		this.extendedPropertyDao = extendedPropertyDao;
	}

	public Map<String, String> getRatingSchemeProperties(long ratingSchemeId) {
		return extendedPropertyDao.getProperties(ratingSchemePropertyTableName, ratingSchemePropertyPrimaryColumnName, ratingSchemeId);
	}

	public void setRatingSchemeProperties(long ratingSchemeId, Map<String, String> props) {
		extendedPropertyDao.updateProperties(ratingSchemePropertyTableName, ratingSchemePropertyPrimaryColumnName, ratingSchemeId, props);
	}
	
	public void deleteRatingSchemeProperties(long ratingSchemeId){
		extendedPropertyDao.deleteProperties(ratingSchemePropertyTableName, ratingSchemePropertyPrimaryColumnName, ratingSchemeId);
	}	
	

	public Map<String, String> getAssessmentSchemeProperties(long assessmentSchemeId) {
		return extendedPropertyDao.getProperties(ratingSchemePropertyTableName, assessmentSchemePropertyPrimaryColumnName, assessmentSchemeId);
	}

	public void setAssessmentSchemeProperties(long assessmentSchemeId, Map<String, String> props) {
		extendedPropertyDao.updateProperties(assessmentSchemePropertyTableName, assessmentSchemePropertyPrimaryColumnName, assessmentSchemeId, props);
	}
	
	public void deleteAssessmentSchemeProperties(long assessmentSchemeId){
		extendedPropertyDao.deleteProperties(assessmentSchemePropertyTableName, assessmentSchemePropertyPrimaryColumnName, assessmentSchemeId);
	}	
	
	
	public Map<String, String> getAssessmentProperties(long assessmentId) {
		return extendedPropertyDao.getProperties(assessmentPropertyTableName, assessmentPropertyPrimaryColumnName, assessmentId);
	}

	public void setAssessmentProperties(long assessmentId, Map<String, String> props) {
		extendedPropertyDao.updateProperties(assessmentPropertyTableName, assessmentPropertyPrimaryColumnName, assessmentId, props);
	}
	
	public void deleteAssessmentProperties(long assessmentId){
		extendedPropertyDao.deleteProperties(assessmentPropertyTableName, assessmentPropertyPrimaryColumnName, assessmentId);
	}	
	
	
	
	public Long nextAssessmentId() {
		return getNextId(assessmentSequencerName);
	}

	public Long nextAssessmentSchemeId() {
		return getNextId(assessmentSchemeSequencerName);
	}
	
	public Long nextRatingSchemeId() {
		return getNextId(ratingSchemeSequencerName);
	}

	public Long nextRatingLevelId() {
		return getNextId(ratingLevelSequencerName);
	}

	public Long nextJobSelectionId() {
		return getNextId("ASSESSMENT_JOB_SELECTION");
	}
	
	public Long nextCompetencySelectionId() {
		return getNextId("ASSESSMENT_COMPETENCY_SELECTION");
	}
	
	public long nextAssessmentSubjectId(){
		return getNextId(assessmentSubjectSequencerName);
	}
	
	
	public List<Long> getRatingSchemeIds(int objectType, long objectId) {
		return getExtendedJdbcTemplate().queryForList(
				getBoundSql("COMPETENCY_ACCESSMENT.SELECT_RATING_SCHEME_IDS_BY_OBJECT_TYPE_AND_OBJECT_ID").getSql(), 
				Long.class,
				new SqlParameterValue(Types.NUMERIC, objectType ),
				new SqlParameterValue(Types.NUMERIC, objectId ));
	}
	
	public int getRatingSchemeCount(int objectType, long objectId) {
		return getExtendedJdbcTemplate().queryForObject(getBoundSql("COMPETENCY_ACCESSMENT.COUNT_RATING_SCHEME_BY_OBJECT_TYPE_AND_OBJECT_ID").getSql(), 
				Integer.class,
				new SqlParameterValue(Types.NUMERIC, objectType ),
				new SqlParameterValue(Types.NUMERIC, objectId ));
	}

	public RatingScheme getRatingSchemeById(long ratingSchemeId) {
		RatingScheme scheme = null;
		try {
			scheme = getExtendedJdbcTemplate().queryForObject(
					getBoundSql("COMPETENCY_ACCESSMENT.SELECT_RATING_SCHEME_BY_ID").getSql(), 
					ratingSchemeMapper, 
					new SqlParameterValue(Types.NUMERIC, ratingSchemeId ) );
			scheme.setProperties(getRatingSchemeProperties(ratingSchemeId));
			scheme.setRatingLevels(getRatingLevels(ratingSchemeId));
			
		} catch (IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1)
	        {
	            log.warn((new StringBuilder()).append("Multiple occurrances of the same ratingScheme ID found: ").append(ratingSchemeId).toString());
	            throw e;
	        }
		} catch (DataAccessException e) {
			 String message = (new StringBuilder()).append("Failure attempting to load ratingScheme by ID : ").append(ratingSchemeId).append(".").toString();
			 log.fatal(message, e);
		}			
		return scheme;
	}

	public List<RatingLevel> getRatingLevels(long ratingSchemeId) {		
		return getExtendedJdbcTemplate().query(
				getBoundSql("COMPETENCY_ACCESSMENT.SELECT_RATING_LEVEL_BY_RATING_SCHEME_ID").getSql(), 
				this.ratingLevelMapper,
				new SqlParameterValue(Types.NUMERIC, ratingSchemeId ));
	}
	
	@Override
	public void saveOrUpdateRatingScheme(RatingScheme ratingScheme) {
		Date now = new Date();
		if(ratingScheme.getRatingSchemeId() > 0){
			// update 
			ratingScheme.setModifiedDate(now);	
			getJdbcTemplate().update(getBoundSql("COMPETENCY_ACCESSMENT.UPDATE_RATING_SCHEME").getSql(),
					new SqlParameterValue (Types.NUMERIC, ratingScheme.getObjectType()),	
					new SqlParameterValue (Types.NUMERIC, ratingScheme.getObjectId()),	
					new SqlParameterValue (Types.VARCHAR, ratingScheme.getName()),						
					new SqlParameterValue (Types.VARCHAR, ratingScheme.getDescription()),	
					new SqlParameterValue (Types.NUMERIC, ratingScheme.getScale()),
					new SqlParameterValue (Types.TIMESTAMP, ratingScheme.getModifiedDate()),
					new SqlParameterValue (Types.NUMERIC, ratingScheme.getRatingSchemeId())
					);
			deleteRatingSchemeProperties(ratingScheme.getRatingSchemeId());
			setRatingSchemeProperties(ratingScheme.getRatingSchemeId(), ratingScheme.getProperties());							
		}else{
			// insert ..
			ratingScheme.setRatingSchemeId(nextRatingSchemeId());
			ratingScheme.setCreationDate(now);
			ratingScheme.setModifiedDate(now);	
			getJdbcTemplate().update(getBoundSql("COMPETENCY_ACCESSMENT.INSERT_RATING_SCHEME").getSql(),
					new SqlParameterValue (Types.NUMERIC, ratingScheme.getRatingSchemeId()),
					new SqlParameterValue (Types.NUMERIC, ratingScheme.getObjectType()),	
					new SqlParameterValue (Types.NUMERIC, ratingScheme.getObjectId()),	
					new SqlParameterValue (Types.VARCHAR, ratingScheme.getName()),						
					new SqlParameterValue (Types.VARCHAR, ratingScheme.getDescription()),	
					new SqlParameterValue (Types.NUMERIC, ratingScheme.getScale()),
					new SqlParameterValue (Types.TIMESTAMP, ratingScheme.getCreationDate()),	
					new SqlParameterValue (Types.TIMESTAMP, ratingScheme.getModifiedDate())				
					);
			if(ratingScheme.getProperties().size() > 0){				
				setRatingSchemeProperties(ratingScheme.getRatingSchemeId(), ratingScheme.getProperties());				
			}
			for(RatingLevel rl : ratingScheme.getRatingLevels())
				rl.setRatingSchemeId(ratingScheme.getRatingSchemeId());
		}
		
		if(ratingScheme.getRatingLevels().size() > 0)
			saveOrUpdateRatingLevels(ratingScheme.getRatingLevels());
		
	} 
	
	public void saveOrUpdateRatingLevels(List<RatingLevel> ratingLevels){	
		final List<RatingLevel> inserts = new ArrayList<RatingLevel>();		
		final List<RatingLevel> updates = new ArrayList<RatingLevel>();				
		for(RatingLevel level : ratingLevels){
			if(level.getRatingLevelId() > 0){
				updates.add(level);
			}else{
				level.setRatingLevelId(this.nextRatingLevelId());
				inserts.add(level);
			}
		}	
		if(inserts.size() > 0){
			getExtendedJdbcTemplate().batchUpdate(				
				getBoundSql("COMPETENCY_ACCESSMENT.INSERT_RATING_LEVEL").getSql(), 
				new BatchPreparedStatementSetter() {					
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						RatingLevel level= inserts.get(i);
						ps.setLong(1, level.getRatingSchemeId());	
						ps.setLong(2, level.getRatingLevelId());
						ps.setString(3,  level.getTitle());
						ps.setInt(4, level.getScore());
					}					
					public int getBatchSize() {
						return inserts.size();
					}
				});		
		}	
		if(updates.size() > 0){
			getExtendedJdbcTemplate().batchUpdate(				
				getBoundSql("COMPETENCY_ACCESSMENT.UPDATE_RATING_LEVEL").getSql(), 
				new BatchPreparedStatementSetter() {					
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						RatingLevel level= updates.get(i);
						ps.setString(1,  level.getTitle());
						ps.setInt(2, level.getScore());
						ps.setLong(3, level.getRatingLevelId());
					}					
					public int getBatchSize() {
						return updates.size();
					}
				});		
		}			
	}

	/**** ASSESSMENT PLAN ******/
	
	public List<Long> getAssessmentIds(int objectType, long objectId) {
		return getExtendedJdbcTemplate().queryForList(
				getBoundSql("COMPETENCY_ACCESSMENT.SELECT_ASSESSMENT_IDS_BY_OBJECT_TYPE_AND_OBJECT_ID").getSql(), 
				Long.class,
				new SqlParameterValue(Types.NUMERIC, objectType ),
				new SqlParameterValue(Types.NUMERIC, objectId ));
	}

	@Override
	public int getAssessmentCount(int objectType, long objectId) {
		return getExtendedJdbcTemplate().queryForObject(getBoundSql("COMPETENCY_ACCESSMENT.COUNT_ASSESSMENT_BY_OBJECT_TYPE_AND_OBJECT_ID").getSql(), 
				Integer.class,
				new SqlParameterValue(Types.NUMERIC, objectType ),
				new SqlParameterValue(Types.NUMERIC, objectId ));
	}

	@Override
	public Assessment getAssessmentById(long assessmentId) {
		Assessment scheme = null;
		try {
			scheme = getExtendedJdbcTemplate().queryForObject(
					getBoundSql("COMPETENCY_ACCESSMENT.SELECT_ASSESSMENT_BY_ID").getSql(), 
					assessmentMapper, 
					new SqlParameterValue(Types.NUMERIC, assessmentId ) );
			
			scheme.setProperties(getAssessmentProperties(assessmentId));	
			
		} catch (IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1)
	        {
	            log.warn((new StringBuilder()).append("Multiple occurrances of the same assessment ID found: ").append(assessmentId).toString());
	            throw e;
	        }
		} catch (DataAccessException e) {
			 String message = (new StringBuilder()).append("Failure attempting to load assessment by ID : ").append(assessmentId).append(".").toString();
			 log.fatal(message, e);
		}			
		return scheme;
	}

	public void saveOrUpdateAssessment(Assessment assessmentScheme) {
		Date now = new Date();
		if(assessmentScheme.getAssessmentId() > 0){
			// update 
			assessmentScheme.setModifiedDate(now);	
			getJdbcTemplate().update(getBoundSql("COMPETENCY_ACCESSMENT.UPDATE_ASSESSMENT").getSql(),
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.getObjectType()),	
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.getObjectId()),	
					new SqlParameterValue (Types.VARCHAR, assessmentScheme.getName()),						
					new SqlParameterValue (Types.VARCHAR, assessmentScheme.getDescription()),	
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.getRatingScheme().getRatingSchemeId()),
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.isMultipleApplyAllowed() ? 1 : 0 ),
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.isFeedbackEnabled() ? 1 : 0 ),					
					new SqlParameterValue (Types.VARCHAR, assessmentScheme.getState().name()),
					new SqlParameterValue (Types.DATE, assessmentScheme.getStartDate()),
					new SqlParameterValue (Types.DATE, assessmentScheme.getEndDate()),
					new SqlParameterValue (Types.TIMESTAMP, assessmentScheme.getModifiedDate()),
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.getAssessmentId())
					);
			deleteAssessmentProperties(assessmentScheme.getAssessmentId());
			setAssessmentProperties(assessmentScheme.getAssessmentId(), assessmentScheme.getProperties());							
		}else{
			// insert ..
			assessmentScheme.setAssessmentId(nextAssessmentId());
			assessmentScheme.setCreationDate(now);
			assessmentScheme.setModifiedDate(now);	
			getJdbcTemplate().update(getBoundSql("COMPETENCY_ACCESSMENT.INSERT_ASSESSMENT").getSql(),
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.getAssessmentId()),
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.getObjectType()),	
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.getObjectId()),	
					new SqlParameterValue (Types.VARCHAR, assessmentScheme.getName()),						
					new SqlParameterValue (Types.VARCHAR, assessmentScheme.getDescription()),	
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.getRatingScheme().getRatingSchemeId()),
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.isMultipleApplyAllowed() ? 1 : 0 ),
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.isFeedbackEnabled() ? 1 : 0 ),					
					new SqlParameterValue (Types.VARCHAR, assessmentScheme.getState().name()),
					new SqlParameterValue (Types.DATE, assessmentScheme.getStartDate()),
					new SqlParameterValue (Types.DATE, assessmentScheme.getEndDate()),					
					new SqlParameterValue (Types.TIMESTAMP, assessmentScheme.getCreationDate()),	
					new SqlParameterValue (Types.TIMESTAMP, assessmentScheme.getModifiedDate())				
					);
			if(assessmentScheme.getProperties().size() > 0){				
				setAssessmentProperties(assessmentScheme.getAssessmentId(), assessmentScheme.getProperties());				
			}
		}	
	}		
	
	
	/**** ASSESSMENT SCHEME ******/
	@Override
	public List<Long> getAssessmentSchemeIds(int objectType, long objectId) {
		return getExtendedJdbcTemplate().queryForList(
				getBoundSql("COMPETENCY_ACCESSMENT.SELECT_ASSESSMENT_SCHEME_IDS_BY_OBJECT_TYPE_AND_OBJECT_ID").getSql(), 
				Long.class,
				new SqlParameterValue(Types.NUMERIC, objectType ),
				new SqlParameterValue(Types.NUMERIC, objectId ));
	}

	@Override
	public int getAssessmentSchemeCount(int objectType, long objectId) {
		return getExtendedJdbcTemplate().queryForObject(getBoundSql("COMPETENCY_ACCESSMENT.COUNT_ASSESSMENT_SCHEME_BY_OBJECT_TYPE_AND_OBJECT_ID").getSql(), 
				Integer.class,
				new SqlParameterValue(Types.NUMERIC, objectType ),
				new SqlParameterValue(Types.NUMERIC, objectId ));
	}

	@Override
	public AssessmentScheme getAssessmentSchemeById(long assessmentSchemeId) {
		AssessmentScheme scheme = null;
		try {
			scheme = getExtendedJdbcTemplate().queryForObject(
					getBoundSql("COMPETENCY_ACCESSMENT.SELECT_ASSESSMENT_SCHEME_BY_ID").getSql(), 
					assessmentSchemeMapper, 
					new SqlParameterValue(Types.NUMERIC, assessmentSchemeId ) );
			
			scheme.setProperties(getAssessmentSchemeProperties(assessmentSchemeId));	
			
		} catch (IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1)
	        {
	            log.warn((new StringBuilder()).append("Multiple occurrances of the same assessmentScheme ID found: ").append(assessmentSchemeId).toString());
	            throw e;
	        }
		} catch (DataAccessException e) {
			 String message = (new StringBuilder()).append("Failure attempting to load assessmentScheme by ID : ").append(assessmentSchemeId).append(".").toString();
			 log.fatal(message, e);
		}			
		return scheme;
	}

	public void saveOrUpdateAssessmentScheme(AssessmentScheme assessmentScheme) {
		Date now = new Date();
		if(assessmentScheme.getAssessmentSchemeId() > 0){
			// update 
			assessmentScheme.setModifiedDate(now);	
			getJdbcTemplate().update(getBoundSql("COMPETENCY_ACCESSMENT.UPDATE_ASSESSMENT_SCHEME").getSql(),
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.getObjectType()),	
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.getObjectId()),	
					new SqlParameterValue (Types.VARCHAR, assessmentScheme.getName()),						
					new SqlParameterValue (Types.VARCHAR, assessmentScheme.getDescription()),	
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.getRatingScheme().getRatingSchemeId()),
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.isMultipleApplyAllowed() ? 1 : 0 ),
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.isFeedbackEnabled() ? 1 : 0 ),
					new SqlParameterValue (Types.TIMESTAMP, assessmentScheme.getModifiedDate()),
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.getAssessmentSchemeId())
					);
			deleteAssessmentSchemeProperties(assessmentScheme.getAssessmentSchemeId());
			setAssessmentSchemeProperties(assessmentScheme.getAssessmentSchemeId(), assessmentScheme.getProperties());							
		}else{
			// insert ..
			assessmentScheme.setAssessmentSchemeId(nextAssessmentSchemeId());
			assessmentScheme.setCreationDate(now);
			assessmentScheme.setModifiedDate(now);	
			getJdbcTemplate().update(getBoundSql("COMPETENCY_ACCESSMENT.INSERT_ASSESSMENT_SCHEME").getSql(),
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.getAssessmentSchemeId()),
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.getObjectType()),	
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.getObjectId()),	
					new SqlParameterValue (Types.VARCHAR, assessmentScheme.getName()),						
					new SqlParameterValue (Types.VARCHAR, assessmentScheme.getDescription()),	
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.getRatingScheme().getRatingSchemeId()),
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.isMultipleApplyAllowed() ? 1 : 0 ),
					new SqlParameterValue (Types.NUMERIC, assessmentScheme.isFeedbackEnabled() ? 1 : 0 ),
					new SqlParameterValue (Types.TIMESTAMP, assessmentScheme.getCreationDate()),	
					new SqlParameterValue (Types.TIMESTAMP, assessmentScheme.getModifiedDate())				
					);
			if(assessmentScheme.getProperties().size() > 0){				
				setAssessmentSchemeProperties(assessmentScheme.getAssessmentSchemeId(), assessmentScheme.getProperties());				
			}
		}	
	}	

	
	public List<Long> getAssessmentJobSelectionIds(int objectType, long objectId) {
		return getExtendedJdbcTemplate().queryForList(
				getBoundSql("COMPETENCY_ACCESSMENT.SELECT_ASSESSMENT_JOB_SELECTION_IDS_BY_OBJECT_TYPE_AND_OBJECT_ID").getSql(), 
				Long.class,
				new SqlParameterValue(Types.NUMERIC, objectType ),
				new SqlParameterValue(Types.NUMERIC, objectId ));
	}


	public int getAssessmentJobSelectionCount(int objectType, long objectId) {
		return getExtendedJdbcTemplate().queryForObject(getBoundSql("COMPETENCY_ACCESSMENT.COUNT_ASSESSMENT_JOB_SELECTION_IDS_BY_OBJECT_TYPE_AND_OBJECT_ID").getSql(), 
				Integer.class,
				new SqlParameterValue(Types.NUMERIC, objectType ),
				new SqlParameterValue(Types.NUMERIC, objectId ));
	}

	public JobSelection getAssessmentJobSelectionById(long subjectId) {
		JobSelection sjb = null;
		try {
			sjb = getExtendedJdbcTemplate().queryForObject(
					getBoundSql("COMPETENCY_ACCESSMENT.SELECT_ASSESSMENT_JOB_SELECTION_BY_ID").getSql(), 
					jobSelectionMapper, 
					new SqlParameterValue(Types.NUMERIC, subjectId ) );					
		} catch (IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1)
	        {
	            log.warn((new StringBuilder()).append("Multiple occurrances of the same JobSelection ID found: ").append(subjectId).toString());
	            throw e;
	        }
		} catch (DataAccessException e) {
			 String message = (new StringBuilder()).append("Failure attempting to load JobSelection by ID : ").append(subjectId).append(".").toString();
			 log.fatal(message, e);
		}			
		return sjb;
	}
	
	
	/**************/
	
	public void removeAssessmentSubjects(final List<Subject> subjects){	
		getExtendedJdbcTemplate().batchUpdate(				
				getBoundSql("COMPETENCY_ACCESSMENT.REMOVE_ASSESSMENT_SUBJECT").getSql(), 
				new BatchPreparedStatementSetter() {					
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						Subject sbj= subjects.get(i);
						ps.setLong(1, sbj.getSubjectId());	
					}					
					public int getBatchSize() {
						return subjects.size();
					}
				});	
	}
	
	public void saveOrUpdateAssessmentSubjects(List<Subject> subjects){	
		final List<Subject> inserts = new ArrayList<Subject>();		
		final List<Subject> updates = new ArrayList<Subject>();				
		for(Subject jobSel : subjects){
			if(jobSel.getSubjectId() > 0){
				updates.add(jobSel);
			}else{
				jobSel.setSubjectId(this.nextAssessmentSubjectId());
				inserts.add(jobSel);
			}
		}	
		if(updates.size() > 0){
			getExtendedJdbcTemplate().batchUpdate(				
				getBoundSql("COMPETENCY_ACCESSMENT.UPDATE_ASSESSMENT_SUBJECT").getSql(), 
				new BatchPreparedStatementSetter() {					
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						Subject sbj= updates.get(i);
						ps.setLong(1, sbj.getSubjectObjectType());	
						ps.setLong(2, sbj.getSubjectObjectId());
						ps.setLong(3, sbj.getSubjectId());
					}					
					public int getBatchSize() {
						return updates.size();
					}
				});		
		}	
		if(inserts.size() > 0){
			getExtendedJdbcTemplate().batchUpdate(				
				getBoundSql("COMPETENCY_ACCESSMENT.INSERT_ASSESSMENT_SUBJECT").getSql(), 
				new BatchPreparedStatementSetter() {					
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						Subject sbj= inserts.get(i);
						ps.setLong(1, sbj.getSubjectId());	
						ps.setInt(2, sbj.getObjectType());	
						ps.setLong(3, sbj.getObjectId());	
						ps.setLong(4, sbj.getSubjectObjectType());	
						ps.setLong(5, sbj.getSubjectObjectId());
					}					
					public int getBatchSize() {
						return inserts.size();
					}
				});		
		}			
	}

	
	public List<Long> getAssessmentSubjectIds(int objectType, long objectId) {
		return getExtendedJdbcTemplate().queryForList(
				getBoundSql("COMPETENCY_ACCESSMENT.SELECT_ASSESSMENT_SUBJECT_IDS_BY_OBJECT_TYPE_AND_OBJECT_ID").getSql(), 
				Long.class,
				new SqlParameterValue(Types.NUMERIC, objectType ),
				new SqlParameterValue(Types.NUMERIC, objectId ));
	}


	public int getAssessmentSubjectCount(int objectType, long objectId) {
		return getExtendedJdbcTemplate().queryForObject(getBoundSql("COMPETENCY_ACCESSMENT.COUNT_ASSESSMENT_SUBJECT_IDS_BY_OBJECT_TYPE_AND_OBJECT_ID").getSql(), 
				Integer.class,
				new SqlParameterValue(Types.NUMERIC, objectType ),
				new SqlParameterValue(Types.NUMERIC, objectId ));
	}

	public Subject getAssessmentSubjectById(long selectionId) {
		Subject jobSel = null;
		try {
			jobSel = getExtendedJdbcTemplate().queryForObject(
					getBoundSql("COMPETENCY_ACCESSMENT.SELECT_ASSESSMENT_SUBJECT_BY_ID").getSql(), 
					subjectMapper, 
					new SqlParameterValue(Types.NUMERIC, selectionId ) );					
		} catch (IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1)
	        {
	            log.warn((new StringBuilder()).append("Multiple occurrances of the same subject ID found: ").append(selectionId).toString());
	            throw e;
	        }
		} catch (DataAccessException e) {
			 String message = (new StringBuilder()).append("Failure attempting to load subject by ID : ").append(selectionId).append(".").toString();
			 log.fatal(message, e);
		}			
		return jobSel;
	}
	
	
	public void removeAssessmentJobSelections(final List<JobSelection> jobSelections){	
		getExtendedJdbcTemplate().batchUpdate(				
				getBoundSql("COMPETENCY_ACCESSMENT.REMOVE_ASSESSMENT_JOB_SELECTION").getSql(), 
				new BatchPreparedStatementSetter() {					
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						JobSelection selection= jobSelections.get(i);
						ps.setLong(1, selection.getSelectionId());	
					}					
					public int getBatchSize() {
						return jobSelections.size();
					}
				});	
	}
	
	public void saveOrUpdateAssessmentJobSelections(List<JobSelection> jobSelections){	
		final List<JobSelection> inserts = new ArrayList<JobSelection>();		
		final List<JobSelection> updates = new ArrayList<JobSelection>();				
		for(JobSelection jobSel : jobSelections){
			if(jobSel.getSelectionId() > 0){
				updates.add(jobSel);
			}else{
				jobSel.setSelectionId(this.nextJobSelectionId());
				inserts.add(jobSel);
			}
		}	
		if(updates.size() > 0){
			getExtendedJdbcTemplate().batchUpdate(				
				getBoundSql("COMPETENCY_ACCESSMENT.UPDATE_ASSESSMENT_JOB_SELECTION").getSql(), 
				new BatchPreparedStatementSetter() {					
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						JobSelection jobSel= updates.get(i);
						ps.setLong(1, jobSel.getClassifyType());	
						ps.setLong(2, jobSel.getClassifiedMajorityId());
						ps.setLong(3, jobSel.getClassifiedMiddleId());
						ps.setLong(4, jobSel.getClassifiedMinorityId());
						ps.setLong(5, jobSel.getJobId());
						ps.setLong(6, jobSel.getSelectionId());
					}					
					public int getBatchSize() {
						return updates.size();
					}
				});		
		}	
		if(inserts.size() > 0){
			getExtendedJdbcTemplate().batchUpdate(				
				getBoundSql("COMPETENCY_ACCESSMENT.INSERT_ASSESSMENT_JOB_SELECTION").getSql(), 
				new BatchPreparedStatementSetter() {					
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						JobSelection jobSel= inserts.get(i);
						ps.setLong(1, jobSel.getSelectionId());	
						ps.setInt(2, jobSel.getObjectType());	
						ps.setLong(3, jobSel.getObjectId());	
						ps.setLong(4, jobSel.getClassifyType());	
						ps.setLong(5, jobSel.getClassifiedMajorityId());
						ps.setLong(6, jobSel.getClassifiedMiddleId());
						ps.setLong(7, jobSel.getClassifiedMinorityId());
						ps.setLong(8, jobSel.getJobId());
					}					
					public int getBatchSize() {
						return inserts.size();
					}
				});		
		}			
	}
}
