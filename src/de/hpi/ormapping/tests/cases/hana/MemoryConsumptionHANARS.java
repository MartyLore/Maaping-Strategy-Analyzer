package de.hpi.ormapping.tests.cases.hana;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import de.hpi.ormapping.structures.ClassNode;
import de.hpi.ormapping.structures.Hierarchy;
import de.hpi.ormapping.tests.Measurement;
import de.hpi.ormapping.tests.cases.MemoryConsumption;

public class MemoryConsumptionHANARS extends MemoryConsumption {

	public MemoryConsumptionHANARS() {
		super();
	}

	public MemoryConsumptionHANARS(String db, Hierarchy hierarchy) throws Exception {
		super(db, hierarchy);
	}

	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	public String getDescription() {
		return super.getDescription();
	}

	@Override
	public String getDatabse() {
		return "hana-row";
	}

	@Override
	public String getQueryTemplate(int strategy, ClassNode node) {

		String template = "SELECT round(sum(ALLOCATED_FIXED_PART_SIZE)/1024) AS SIZE FROM M_RS_TABLES WHERE SCHEMA_NAME='"+schemaName+"' AND TABLE_NAME='%1$s_";

		if (strategy == ST_Strategy) {
			template = String.format(template, "ST");
		} else if (strategy == TPC_Strategy) {
			template = String.format(template, "TPC");
		} else if (strategy == TPCC_Strategy) {
			template = String.format(template, "TPCC");
		}
		
		template += "%1$s'";

		return template;
	}

	@Override
	public Map<String, Measurement> executeForST(int repetitions) throws Exception {
		return execute(repetitions, ST_Strategy);
	}

	@Override
	public Map<String, Measurement> executeForTPC(int repetitions) throws Exception {
		return execute(repetitions, TPC_Strategy);
	}

	@Override
	public Map<String, Measurement> executeForTPCC(int repetitions) throws Exception {
		return execute(repetitions, TPCC_Strategy);
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
	}

	@Override
	public void tearDown() {
		// TODO Auto-generated method stub
	}

	private Map<String, Measurement> execute(int repetitions, int strategy) throws Exception {

		Map<String, Measurement> result = new HashMap<>();
		Statement stmt = cn.createStatement();

		for (ClassNode node : hierarchy.classList) {

			String sql = getQueryTemplate(strategy, null);
			sql = String.format(sql, node.className.toUpperCase());

			ResultSet rs = stmt.executeQuery(sql);

			Measurement memoryMeasurement = new Measurement();
			while (rs.next()) {
				memoryMeasurement.minValue = rs.getLong(1);
				memoryMeasurement.avgValue = rs.getLong(1);
				memoryMeasurement.maxValue = rs.getLong(1);
			}

			result.put(node.className, memoryMeasurement);
		}

		return result;
	}

}
