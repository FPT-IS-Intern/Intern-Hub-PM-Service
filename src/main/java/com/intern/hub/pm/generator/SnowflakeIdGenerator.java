package com.intern.hub.pm.generator;

import com.intern.hub.library.common.utils.Snowflake;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdGenerator implements IdentifierGenerator {

    private final Snowflake snowflake;

    public SnowflakeIdGenerator(Snowflake snowflake) {
        this.snowflake = snowflake;
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {
        return snowflake.next();
    }
}
