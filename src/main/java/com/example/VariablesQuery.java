package com.example;

import static org.kie.server.remote.rest.common.util.RestUtils.createResponse;
import static org.kie.server.remote.rest.common.util.RestUtils.getContentType;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.jbpm.kie.services.impl.query.SqlQueryDefinition;
import org.jbpm.kie.services.impl.query.mapper.UserTaskInstanceWithCustomVarsQueryMapper;
import org.jbpm.services.api.model.UserTaskInstanceWithVarsDesc;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.api.query.model.QueryDefinition;
import org.kie.api.runtime.query.QueryContext;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.definition.QueryFilterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("server/queries/variables")
public class VariablesQuery {

	private static final String VARIABLES_QUERY_NAME = "VARIABLES_QUERY_NAME";

	private static final Logger logger = LoggerFactory.getLogger(VariablesQuery.class);

	private QueryService queryService;

	public VariablesQuery(QueryService queryService) {
		this.queryService = queryService;
	}

	@GET
	@Path("/")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response tasksVariables(@Context HttpHeaders headers, @QueryParam("vars") List<String> vars) {
		logger.info("VariablesQuery.tasksVariables()");
		Variant v = getVariant(headers);
		String contentType = getContentType(headers);

		MarshallingFormat format = MarshallingFormat.fromType(contentType);
		if (format == null) {
			format = MarshallingFormat.valueOf(contentType);
		}
		try {
			// TODO: change the datasource
			QueryDefinition queryDefinition = new SqlQueryDefinition(VARIABLES_QUERY_NAME,
					"java:jboss/datasources/ExampleDS", QueryDefinition.Target.CUSTOM);

			Map<String, String> variableMap = new HashMap<>();
			String variableColumns = "";
			for (String var : vars) {
				variableColumns += String
						.format(", MAX ( CASE V.VARIABLEINSTANCEID WHEN '%s' THEN V.VALUE END ) AS VAR_%s", var, var);
				variableMap.put("VAR_" + var, "String");
			}

			// , MAX ( CASE V.VARIABLEINSTANCEID WHEN 'name' THEN V.VALUE END ) AS VAR_NAME,
			// MAX ( CASE V.VARIABLEINSTANCEID WHEN 'age' THEN V.VALUE END ) AS VAR_AGE"
			queryDefinition.setExpression(
					"SELECT T.taskId, T.status, T.activationTime, T.name, T.description, T.priority, T.actualOwner, T.createdBy, T.deploymentId, T.processId, T.processInstanceId, T.createdOn, T.dueDate"
							+ variableColumns + " FROM VARIABLEINSTANCELOG AS V"
							+ " LEFT JOIN VARIABLEINSTANCELOG AS V2 ON ( V.VARIABLEINSTANCEID = V2.VARIABLEINSTANCEID AND V.PROCESSINSTANCEID=V2.PROCESSINSTANCEID AND V.ID < V2.ID )"
							+ " INNER JOIN AUDITTASKIMPL AS T ON T.PROCESSINSTANCEID = V.PROCESSINSTANCEID"
							+ " WHERE V2.ID IS NULL" + " GROUP BY T.TASKID");
			queryService.replaceQuery(queryDefinition);

			List<UserTaskInstanceWithVarsDesc> list = queryService.query(VARIABLES_QUERY_NAME,
					UserTaskInstanceWithCustomVarsQueryMapper.get(variableMap), new QueryContext());

			if (logger.isDebugEnabled()) {
				list.forEach(ut -> logger.debug("> " + ut + " vars " + ut.getVariables()));
			}

			Marshaller marshaller = MarshallerFactory.getMarshaller(format, this.getClass().getClassLoader());
			String result = marshaller.marshall(list);

			return createResponse(result, v, Response.Status.OK);
		} catch (Exception e) {
			// in case marshalling failed return the call container response to
			// keep backward compatibility
			String response = "Execution failed with error : " + e.getMessage();
			logger.debug("Returning Failure response with content '{}'", response);
			return createResponse(response, v, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@POST
	@Path("/")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response tasksVariablesFilter(@Context HttpHeaders headers, @QueryParam("vars") List<String> vars,
			@QueryParam("filters") String filters) {
		logger.info("VariablesQuery.tasksVariables()");
		Variant v = getVariant(headers);
		String contentType = getContentType(headers);

		MarshallingFormat format = MarshallingFormat.fromType(contentType);

		if (format == null) {
			format = MarshallingFormat.valueOf(contentType);
		}

		Marshaller marshaller = MarshallerFactory.getMarshaller(format, this.getClass().getClassLoader());
		QueryFilterSpec filterSpec = marshaller.unmarshall(filters, QueryFilterSpec.class);

		try {
			// TODO: change the datasource
			QueryDefinition queryDefinition = new SqlQueryDefinition(VARIABLES_QUERY_NAME,
					"java:jboss/datasources/ExampleDS", QueryDefinition.Target.CUSTOM);

			Map<String, String> variableMap = new HashMap<>();
			String variableColumns = "";
			for (String var : vars) {
				variableColumns += String
						.format(", MAX ( CASE V.VARIABLEINSTANCEID WHEN '%s' THEN V.VALUE END ) AS VAR_%s", var, var);
				variableMap.put("VAR_" + var, "String");
			}

			queryDefinition.setExpression(
					"SELECT T.taskId, T.status, T.activationTime, T.name, T.description, T.priority, T.actualOwner, T.createdBy, T.deploymentId, T.processId, T.processInstanceId, T.createdOn, T.dueDate"
							+ variableColumns + " FROM VARIABLEINSTANCELOG AS V"
							+ " LEFT JOIN VARIABLEINSTANCELOG AS V2 ON ( V.VARIABLEINSTANCEID = V2.VARIABLEINSTANCEID AND V.PROCESSINSTANCEID=V2.PROCESSINSTANCEID AND V.ID < V2.ID )"
							+ " INNER JOIN AUDITTASKIMPL AS T ON T.PROCESSINSTANCEID = V.PROCESSINSTANCEID"
							+ " WHERE V2.ID IS NULL" + " GROUP BY T.TASKID");
			queryService.replaceQuery(queryDefinition);

			org.jbpm.services.api.query.model.QueryParam[] params = new org.jbpm.services.api.query.model.QueryParam[0];

			if (filterSpec.getParameters() != null) {
				params = new org.jbpm.services.api.query.model.QueryParam[filterSpec.getParameters().length];
				int index = 0;
				for (org.kie.server.api.model.definition.QueryParam param : filterSpec.getParameters()) {
					params[index] = new org.jbpm.services.api.query.model.QueryParam(param.getColumn(),
							param.getOperator(), param.getValue());
					index++;
				}
			}

			List<UserTaskInstanceWithVarsDesc> list = queryService.query(VARIABLES_QUERY_NAME,
					UserTaskInstanceWithCustomVarsQueryMapper.get(variableMap), new QueryContext(), params);

			if (logger.isDebugEnabled()) {
				list.forEach(ut -> logger.debug("> " + ut + " vars " + ut.getVariables()));
			}

			String result = marshaller.marshall(list);

			return createResponse(result, v, Response.Status.OK);
		} catch (Exception e) {
			// in case marshalling failed return the call container response to
			// keep backward compatibility
			String response = "Execution failed with error : " + e.getMessage();
			logger.debug("Returning Failure response with content '{}'", response);
			return createResponse(response, v, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}
