package com.bonsai.pantryghost.data

import com.bonsai.pantryghost.data.dao.FoodDao
import com.bonsai.pantryghost.data.dao.FoodTagDao
import com.bonsai.pantryghost.data.dao.FoodTagJoinDao
import com.bonsai.pantryghost.data.dao.MealDao
import com.bonsai.pantryghost.data.dao.MealTimeDao
import com.bonsai.pantryghost.data.dao.MealTypeDao
import com.bonsai.pantryghost.data.dao.ServingAmountDao
import com.bonsai.pantryghost.data.dao.ServingDao
import com.bonsai.pantryghost.model.Food
import com.bonsai.pantryghost.model.FoodTag
import com.bonsai.pantryghost.model.FoodTagJoin
import com.bonsai.pantryghost.model.Meal
import com.bonsai.pantryghost.model.MealTime
import com.bonsai.pantryghost.model.MealType
import com.bonsai.pantryghost.model.Serving
import com.bonsai.pantryghost.model.ServingAmount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.ZoneId

class DaoRepository(
    val foodDao: FoodDao,
    val servingAmountDao: ServingAmountDao,
    val mealTimeDao: MealTimeDao,
    val mealTypeDao: MealTypeDao,
    val servingDao: ServingDao,
    val mealDao: MealDao,
    val foodTagDao: FoodTagDao,
    val foodTagJoinDao: FoodTagJoinDao
) : DataRepository {

    // food
    override fun getAllFoods(): Flow<List<Food>> = foodDao.getAll()
    override fun getFoodById(id: Int): Flow<Food> = foodDao.getById(id)
    override suspend fun insertFood(food: Food) = foodDao.insert(food).toInt()
    override suspend fun updateFood(food: Food) = foodDao.update(food)
    override suspend fun insertFoods(foods: List<Food>) = foodDao.insertAll(foods)
    override suspend fun deleteFood(food: Food) = foodDao.delete(food)

    // meal time
    override fun getAllMealTimes(): Flow<List<MealTime>> = mealTimeDao.getAll()
    override fun getMealTimeById(id: Int): Flow<MealTime> = mealTimeDao.getById(id)
    override fun getMealTimesOnDate(date: LocalDate): Flow<List<MealTime>> {
        val (start, end) = date.toDayBounds()
        return mealTimeDao.getMealsOnDate(start, end)
    }

    override suspend fun insertMealTime(mealTime: MealTime): Int =
        mealTimeDao.insert(mealTime).toInt()

    override suspend fun insertMealTimes(sampleMealTimes: List<MealTime>) =
        mealTimeDao.insertAll(sampleMealTimes)

    override suspend fun updateMealTime(mealTime: MealTime) = mealTimeDao.update(mealTime)

    // meal type
    override fun getMealTypeById(mealTypeId: Int): Flow<MealType> =
        mealTypeDao.getById(mealTypeId)

    override fun getAllMealTypes(): Flow<List<MealType>> = mealTypeDao.getAll()
    override suspend fun insertMealTypes(mealTypes: List<MealType>) =
        mealTypeDao.insertAll(mealTypes)

    // serving amount
    override fun getServingAmountsById(id: Int): Flow<List<ServingAmount>> =
        servingAmountDao.getByMealTimeId(id)

    override fun getAllServingAmounts(): Flow<List<ServingAmount>> = servingAmountDao.getAll()
    override suspend fun insertServingAmounts(servingAmounts: List<ServingAmount>) =
        servingAmountDao.insertAll(servingAmounts)

    override suspend fun updateServingAmounts(servingAmounts: List<ServingAmount>) =
        servingAmountDao.updateAll(servingAmounts)

    override suspend fun deleteServingAmount(servingAmount: ServingAmount) =
        servingAmountDao.delete(servingAmount)

    // meal
    override fun getMealsOnDate(date: LocalDate): Flow<List<Meal>> {
        val (start, end) = date.toDayBounds()
        return mealDao.getMealsOnDate(start, end)
    }

    // serving
    override fun getServingsOnDate(date: LocalDate): Flow<List<Serving>> {
        val (start, end) = date.toDayBounds()
        return servingDao.getServingsOnDate(start, end)
    }

    // food tag
    override fun getTagsByFoodId(foodId: Int): Flow<List<FoodTag>> =
        foodTagJoinDao.getTagsByFoodId(foodId)

    override fun getFoodsByTagName(tagName: String): Flow<List<Food>> {
        foodTagJoinDao.getFoodsByTagName(tagName)
    }

    override suspend fun insertFoodTagOrGetId(tag: String): Int =
        foodTagDao.getByName(tag).firstOrNull()?.id ?:
        foodTagDao.insert(FoodTag(0, tag)).toInt()

    override suspend fun insertFoodTagJoin(foodTagJoin: FoodTagJoin) =
        foodTagJoinDao.insert(foodTagJoin)

    override suspend fun addTagToFood(foodId: Int, tagName: String) {
        val foodTagId = insertFoodTagOrGetId(tagName)
        insertFoodTagJoin(FoodTagJoin(foodId, foodTagId))
    }

    override suspend fun removeTagFromFood(foodId: Int, tagId: Int) {
        foodTagJoinDao.delete(FoodTagJoin(foodId, tagId))
        foodTagDao.deleteUnusedTags()
    }
}

private data class DayBounds(val start: Long, val end: Long)

private fun LocalDate.toDayBounds(): DayBounds {
    val start = this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val end = this.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    return DayBounds(start, end)
}
