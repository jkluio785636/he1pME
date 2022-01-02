package Service;

import SpecialDataStructure.UrlType;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Map;

public class CommonService {
    private final DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.standard().build());

    protected String getIdFromDB(final String searchValue) {
        final Map<String, String> nameMap = Collections.singletonMap("#key", "name");
        final Map<String, Object> valueMap = Collections.singletonMap(":value", searchValue);
        final QuerySpec querySpec = new QuerySpec().withKeyConditionExpression("#key = :value")
                .withNameMap(nameMap).withValueMap(valueMap);
        final ItemCollection<QueryOutcome> items = dynamoDB.getTable("AllOfId").query(querySpec);

        if (!items.iterator().hasNext()) {
            return null;
        }

        final Item item = items.iterator().next();
        return item.getString("id");
    }

    protected String getUrlFromDB(final String searchValue, final UrlType type) {
        final Map<String, String> nameMap = Collections.singletonMap("#key", "name");
        final Map<String, Object> valueMap = Collections.singletonMap(":value", searchValue);
        final QuerySpec querySpec = new QuerySpec().withKeyConditionExpression("#key = :value")
                .withNameMap(nameMap).withValueMap(valueMap);
        final ItemCollection<QueryOutcome> items = dynamoDB.getTable("Url").query(querySpec);

        if (!items.iterator().hasNext()) {
            return null;
        }

        final Item item = items.iterator().next();
        return StringUtils.equals(item.getString("type"), type.getType()) ? item.getString("url") : null;
    }

    protected void replyByHe1pMETemplate(final MessageChannel messageChannel, final String msg) {
        final Color color = Color.of(255, 192, 203);
        messageChannel.createMessage(EmbedCreateSpec.create().withTitle(msg).withColor(color)).block();
    }
}
