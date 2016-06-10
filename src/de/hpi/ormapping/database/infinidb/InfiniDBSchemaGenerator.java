package de.hpi.ormapping.database.infinidb;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.hpi.ormapping.database.ConnectionFactory;
import de.hpi.ormapping.database.SchemaGenerator;
import de.hpi.ormapping.structures.ClassAttribute;
import de.hpi.ormapping.structures.ClassNode;
import de.hpi.ormapping.structures.Hierarchy;

public class InfiniDBSchemaGenerator implements SchemaGenerator {

	public static String connection_identifier = "infinidb";

	private static Map<String, String> attributeMappings = new HashMap<String, String>();
	static {
		attributeMappings.put("STRING", "VARCHAR(45)");
		attributeMappings.put("INTEGER", "INTEGER");
		attributeMappings.put("DOUBLE", "DOUBLE");
		attributeMappings.put("DATE", "DATE");
		attributeMappings.put("TIME", "TIMESTAMP");
	}

	public InfiniDBSchemaGenerator(String db) {
		if (db != null) {
			connection_identifier = db;
		}
	}

	@Override
	public void dropSchema() throws Exception {
		
		Connection cn = ConnectionFactory.getConnection(connection_identifier);
		Statement stmt = cn.createStatement();
		
		stmt.executeUpdate(String.format("DROP SCHEMA IF EXISTS %1s", getFullSchemaName()));
		stmt.executeUpdate(String.format("CREATE SCHEMA IF NOT EXISTS %1s", getFullSchemaName()));
		stmt.close();
		cn.close();
	}
	
	@Override
	public void generateSTSchema(Hierarchy hierarchy) throws Exception {

		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Create ST Table for " + connection_identifier);

		Connection cn = ConnectionFactory.getConnection(connection_identifier);
		Statement stmt = cn.createStatement();

		// generate SQL for single table mapping
		String sql = generateSQLForST(hierarchy);
		stmt.executeUpdate(sql);
		stmt.close();
		cn.close();

	}

	@Override
	public void generateTPCSchema(Hierarchy hierarchy) throws Exception {

		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Create TPC Tables for " + connection_identifier);

		Connection cn = ConnectionFactory.getConnection(connection_identifier);
		Statement stmt = cn.createStatement();

		for (ClassNode node : hierarchy.classList) {

			if (!node.attributes.isEmpty()) {

				// generate SQL for single table mapping
				String sql = generateSQLForTPC(node);
				stmt.executeUpdate(sql);
			}
		}

		stmt.close();
		cn.close();

	}

	@Override
	public void generateTPCCSchema(Hierarchy hierarchy) throws Exception {
		
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Create TPCC Tables for " + connection_identifier);

		Connection cn = ConnectionFactory.getConnection(connection_identifier);
		Statement stmt = cn.createStatement();

		for (ClassNode node : hierarchy.classList) {

			if (!node.type.equals("{abstract}")) {

				// generate SQL for single table mapping
				String sql = generateSQLForTPCC(node, hierarchy);
				stmt.executeUpdate(sql);

			}
		}

		stmt.close();
		cn.close();
	}
	
	@Override
	public String getFullSchemaName() {
		return SchemaGenerator.SCHEMA_NAME;
	}
	
	/**
	 * 
	 * SQL generator methods
	 * 
	 */
	
	private String generateSQLForST(Hierarchy hierarchy) {
		
		StringBuilder buffer = new StringBuilder(String.format("CREATE TABLE %1s.ST_%2s ( ", getFullSchemaName(), hierarchy.classList.get(0).className.toUpperCase()));
		buffer.append("ID INTEGER NOT NULL PRIMARY KEY,");
		
		for (ClassNode node : hierarchy.classList) {
			for (ClassAttribute att : node.attributes) {
				buffer.append(generateAttributeDefinition(att.attName, att.attType));
			}
		}

		buffer.append(" TYPE VARCHAR(16));");
		
		return buffer.toString();
	}
	
	private String generateSQLForTPC(ClassNode node) {
		
		StringBuilder buffer = new StringBuilder(String.format("CREATE TABLE %1s.TPC_%2s ( ", getFullSchemaName(), node.className.toUpperCase()));
		buffer.append("ID INTEGER NOT NULL PRIMARY KEY,");
		
		for (ClassAttribute att : node.attributes) {
			buffer.append(generateAttributeDefinition(att.attName, att.attType));
		}

		buffer.append(" TYPE VARCHAR(16));");
		
		return buffer.toString();
	}
	
	private String generateSQLForTPCC(ClassNode node, Hierarchy hierarchy) {
		
		StringBuilder buffer = new StringBuilder(String.format("CREATE TABLE %1s.TPCC_%2s ( ", getFullSchemaName(), node.className.toUpperCase()));

		buffer.append("ID INTEGER NOT NULL PRIMARY KEY,");
		
		for (ClassAttribute att : node.collectAllInheritedAttributes(hierarchy)) {
			buffer.append(generateAttributeDefinition(att.attName, att.attType));
		}

		buffer.setLength(buffer.length() - 2);
		buffer.append(");");
		
		return buffer.toString();
	}

	private String generateAttributeDefinition(String attributeName, String attributeType) {
		return String.format("%1s %2s DEFAULT NULL, ", attributeName.toUpperCase(), attributeMappings.get(attributeType));
	}
}
