package ci.us.zhixing.app.domain.units

/** Whether a category can be converted with a single scale factor. */
internal enum class UnitConversionKind {
    LINEAR,
    AFFINE,
}

/** One of the legacy calculator's nine unit-conversion categories. */
internal enum class UnitCategory(
    val id: String,
    val displayName: String,
    val dimension: UnitDimension,
    val conversionKind: UnitConversionKind,
) {
    LENGTH(
        id = "length",
        displayName = "长度",
        dimension = UnitDimension(lengthExponent = 1),
        conversionKind = UnitConversionKind.LINEAR,
    ),
    AREA(
        id = "area",
        displayName = "面积",
        dimension = UnitDimension(lengthExponent = 2),
        conversionKind = UnitConversionKind.LINEAR,
    ),
    VOLUME(
        id = "volume",
        displayName = "体积",
        dimension = UnitDimension(lengthExponent = 3),
        conversionKind = UnitConversionKind.LINEAR,
    ),
    MASS(
        id = "mass",
        displayName = "质量",
        dimension = UnitDimension(massExponent = 1),
        conversionKind = UnitConversionKind.LINEAR,
    ),
    TEMPERATURE(
        id = "temp",
        displayName = "温度",
        dimension = UnitDimension(temperatureExponent = 1),
        conversionKind = UnitConversionKind.AFFINE,
    ),
    SPEED(
        id = "speed",
        displayName = "速度",
        dimension = UnitDimension(lengthExponent = 1, timeExponent = -1),
        conversionKind = UnitConversionKind.LINEAR,
    ),
    TIME(
        id = "time",
        displayName = "时间",
        dimension = UnitDimension(timeExponent = 1),
        conversionKind = UnitConversionKind.LINEAR,
    ),
    DATA_STORAGE(
        id = "data",
        displayName = "数据存储",
        dimension = UnitDimension(dataExponent = 1),
        conversionKind = UnitConversionKind.LINEAR,
    ),
    ANGLE(
        id = "angle",
        displayName = "角度",
        dimension = UnitDimension(angleExponent = 1),
        conversionKind = UnitConversionKind.LINEAR,
    ),
}
