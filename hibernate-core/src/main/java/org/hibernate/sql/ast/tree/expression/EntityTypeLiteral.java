/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.tree.expression;

import java.util.List;

import org.hibernate.cache.MutableCacheKeyBuilder;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.IndexedConsumer;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.MappingModelExpressible;
import org.hibernate.persister.entity.DiscriminatorType;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.query.sqm.sql.internal.DomainResultProducer;
import org.hibernate.sql.ast.SqlAstWalker;
import org.hibernate.sql.ast.spi.SqlSelection;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.basic.BasicResult;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.java.JavaTypedExpressible;

/**
 * @author Steve Ebersole
 */
public class EntityTypeLiteral
		implements Expression, MappingModelExpressible<Object>, DomainResultProducer<Object>, JavaTypedExpressible<Object> {
	private final EntityPersister entityTypeDescriptor;
	private final DiscriminatorType<?> discriminatorType;

	public EntityTypeLiteral(EntityPersister entityTypeDescriptor) {
		this.entityTypeDescriptor = entityTypeDescriptor;
		this.discriminatorType = (DiscriminatorType) ( (Queryable) entityTypeDescriptor ).getTypeDiscriminatorMetadata().getResolutionType();
	}

	public EntityPersister getEntityTypeDescriptor() {
		return entityTypeDescriptor;
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// MappingModelExpressible

	@Override
	public MappingModelExpressible getExpressionType() {
		return this;
	}

	@Override
	public int getJdbcTypeCount() {
		return discriminatorType.getJdbcTypeCount();
	}

	@Override
	public List<JdbcMapping> getJdbcMappings() {
		return discriminatorType.getJdbcMappings();
	}

	@Override
	public JdbcMapping getJdbcMapping(int index) {
		return discriminatorType.getJdbcMapping( index );
	}

	@Override
	public JdbcMapping getSingleJdbcMapping() {
		return discriminatorType.getSingleJdbcMapping();
	}

	@Override
	public int forEachJdbcType(int offset, IndexedConsumer<JdbcMapping> action) {
		return discriminatorType.forEachJdbcType( offset, action );
	}

	@Override
	public Object disassemble(Object value, SharedSessionContractImplementor session) {
		return discriminatorType.disassemble( value, session );
	}

	@Override
	public void addToCacheKey(MutableCacheKeyBuilder cacheKey, Object value, SharedSessionContractImplementor session) {
		discriminatorType.addToCacheKey( cacheKey, value, session );
	}

	@Override
	public <X, Y> int forEachDisassembledJdbcValue(
			Object value,
			int offset,
			X x,
			Y y,
			JdbcValuesBiConsumer<X, Y> valuesConsumer,
			SharedSessionContractImplementor session) {
		return discriminatorType.forEachDisassembledJdbcValue( value, offset, x, y, valuesConsumer, session );
	}

	@Override
	public <X, Y> int forEachJdbcValue(
			Object value,
			int offset,
			X x,
			Y y,
			JdbcValuesBiConsumer<X, Y> valuesConsumer,
			SharedSessionContractImplementor session) {
		return discriminatorType.forEachJdbcValue( value, offset, x, y, valuesConsumer, session );
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// DomainResultProducer

	@Override
	public void applySqlSelections(DomainResultCreationState creationState) {
		createSqlSelection( creationState );
	}

	@Override
	public DomainResult<Object> createDomainResult(String resultVariable, DomainResultCreationState creationState) {
		return new BasicResult<>(
				createSqlSelection( creationState ).getValuesArrayPosition(),
				resultVariable,
				discriminatorType
		);
	}

	private SqlSelection createSqlSelection(DomainResultCreationState creationState) {
		return creationState.getSqlAstCreationState().getSqlExpressionResolver().resolveSqlSelection(
				this,
				discriminatorType.getJdbcJavaType(),
				null,
				creationState.getSqlAstCreationState().getCreationContext().getMappingMetamodel().getTypeConfiguration()
		);
	}

	@Override
	public void accept(SqlAstWalker sqlTreeWalker) {
		sqlTreeWalker.visitEntityTypeLiteral( this );
	}

	@Override
	public JavaType getExpressibleJavaType() {
		return discriminatorType.getExpressibleJavaType();
	}
}
