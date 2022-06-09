package com.safecornerscoffee.microservices.core.recommendation.mapper;

import com.safecornerscoffee.microservices.api.core.recommendation.Recommendation;
import com.safecornerscoffee.microservices.core.recommendation.entity.RecommendationEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationMapperTest {

    RecommendationMapper mapper = Mappers.getMapper(RecommendationMapper.class);

    @Test
    void mapperTests() {

        assertThat(mapper).isNotNull();

        Recommendation api = new Recommendation(1, 2, "a", 4, "c", "sa");

        RecommendationEntity entity = mapper.apiToEntity(api);

        assertThat(entity.getProductId()).isEqualTo(api.getProductId());
        assertThat(entity.getRecommendationId()).isEqualTo(api.getRecommendationId());
        assertThat(entity.getAuthor()).isEqualTo(api.getAuthor());
        assertThat(entity.getRating()).isEqualTo(api.getRate());
        assertThat(entity.getContent()).isEqualTo(api.getContent());

        Recommendation api2 = mapper.entityToApi(entity);

        assertThat(api2.getProductId()).isEqualTo(api.getProductId());
        assertThat(api2.getRecommendationId()).isEqualTo(api.getRecommendationId());
        assertThat(api2.getAuthor()).isEqualTo(api.getAuthor());
        assertThat(api2.getRate()).isEqualTo(api.getRate());
        assertThat(api2.getContent()).isEqualTo(api.getContent());
        assertThat(api2.getServiceAddress()).isNull();

    }

    @Test
    void mapperListTests() {

        assertThat(mapper).isNotNull();

        Recommendation api = new Recommendation(1, 2, "a", 4, "c", "sa");
        List<Recommendation> apiList = Collections.singletonList(api);

        List<RecommendationEntity> entityList = mapper.apiListToEntityList(apiList);
        assertThat(entityList).hasSameSizeAs(apiList);

        RecommendationEntity entity = entityList.get(0);
        assertThat(entity.getProductId()).isEqualTo(api.getProductId());
        assertThat(entity.getRecommendationId()).isEqualTo(api.getRecommendationId());
        assertThat(entity.getAuthor()).isEqualTo(api.getAuthor());
        assertThat(entity.getRating()).isEqualTo(api.getRate());
        assertThat(entity.getContent()).isEqualTo(api.getContent());

        List<Recommendation> api2List = mapper.entityListToApiList(entityList);
        assertThat(api2List).hasSameSizeAs(apiList);

        Recommendation api2 = api2List.get(0);

        assertThat(api2.getProductId()).isEqualTo(api.getProductId());
        assertThat(api2.getRecommendationId()).isEqualTo(api.getRecommendationId());
        assertThat(api2.getAuthor()).isEqualTo(api.getAuthor());
        assertThat(api2.getRate()).isEqualTo(api.getRate());
        assertThat(api2.getContent()).isEqualTo(api.getContent());
        assertThat(api2.getServiceAddress()).isNull();

    }
}