package org.yolok.he1pME.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@DynamoDBTable(tableName = "BadWord")
public class BadWord {

    @DynamoDBHashKey
    private String word;

    @DynamoDBAttribute(attributeName = "guild_id")
    private String guildId;
}
