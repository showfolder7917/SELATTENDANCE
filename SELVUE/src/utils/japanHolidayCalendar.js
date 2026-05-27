// 日本节假日计算统一收口在这里，供排班表头、后续周视图和日报摘要复用同一套口径。

// 把 yyyy-mm-dd 文本拆成年月日，保持前端只依赖后端回传的日期字符串，不额外引入时区库。
function parseDateParts(dateText) {
  // 分割固定格式日期，后续所有节假日规则都直接基于数字年月日判断。
  const [yearText, monthText, dayText] = String(dateText || '').split('-')
  // 返回数字化年月日，供后续业务规则直接比较。
  return {
    year: Number(yearText),
    month: Number(monthText),
    day: Number(dayText)
  }
}

// 固定用本地日期对象读取星期序号，避免 UTC 偏移把周末判断错一天。
function toLocalDate(parts) {
  // Date 构造器用零点本地时间生成目标日历日，周几判断就不会被浏览器时区换算干扰。
  return new Date(parts.year, parts.month - 1, parts.day)
}

// “第 N 个星期几”节日统一通过这个工具算出日号，覆盖成人之日、海之日、敬老日、体育之日。
function nthWeekdayOfMonth(year, month, weekday, occurrence) {
  // 先拿到当月第一天的星期序号，后面再按偏移量推算第 N 次出现。
  const firstWeekday = new Date(year, month - 1, 1).getDay()
  // firstOffset 表示从 1 号往后推多少天才会落到目标星期。
  const firstOffset = (weekday - firstWeekday + 7) % 7
  // 返回该节日在当月中的具体日号，供主规则表直接比较。
  return 1 + firstOffset + (occurrence - 1) * 7
}

// 春分日按日本常见近似公式计算，当前第一阶段只覆盖现代年度，精度已足够前端展示。
function getVernalEquinoxDay(year) {
  // 2000 年以后采用现代近似公式，结果向下取整即可落到正式日历日。
  return Math.floor(20.8431 + 0.242194 * (year - 2000) - Math.floor((year - 2000) / 4))
}

// 秋分日同样按现代常用近似公式生成，满足排班表头的红日子展示精度。
function getAutumnalEquinoxDay(year) {
  // 2000 年以后按日本公开近似规则计算，足以支撑工作台月视图展示。
  return Math.floor(23.2488 + 0.242194 * (year - 2000) - Math.floor((year - 2000) / 4))
}

// 基础法定节假日先按“当天是否命中”生成，不在这一层处理振替休日和国民之休日。
function getBaseHolidayKey(parts) {
  // 没拿到有效年份时直接返回空，避免异常值污染表头样式。
  if (!parts.year || !parts.month || !parts.day) {
    return ''
  }
  // 先构造简短变量，下面的节日规则会更易读。
  const { year, month, day } = parts

  // 元日固定在 1 月 1 日，是全年第一天的红日子入口。
  if (month === 1 && day === 1) {
    return 'holidayNewYear'
  }
  // 成人之日自 2000 年起固定为 1 月第二个星期一。
  if (month === 1 && day === nthWeekdayOfMonth(year, 1, 1, 2)) {
    return 'holidayComingOfAgeDay'
  }
  // 建国纪念日固定在 2 月 11 日。
  if (month === 2 && day === 11) {
    return 'holidayNationalFoundationDay'
  }
  // 天皇诞生日当前制度下固定在 2 月 23 日。
  if (month === 2 && day === 23) {
    return 'holidayEmperorBirthday'
  }
  // 春分日每年略有浮动，需要按近似公式取值。
  if (month === 3 && day === getVernalEquinoxDay(year)) {
    return 'holidayVernalEquinox'
  }
  // 昭和之日固定在 4 月 29 日。
  if (month === 4 && day === 29) {
    return 'holidayShowaDay'
  }
  // 宪法纪念日固定在 5 月 3 日。
  if (month === 5 && day === 3) {
    return 'holidayConstitutionMemorialDay'
  }
  // 绿之日固定在 5 月 4 日。
  if (month === 5 && day === 4) {
    return 'holidayGreeneryDay'
  }
  // 儿童之日固定在 5 月 5 日。
  if (month === 5 && day === 5) {
    return 'holidayChildrensDay'
  }
  // 海之日为 7 月第三个星期一。
  if (month === 7 && day === nthWeekdayOfMonth(year, 7, 1, 3)) {
    return 'holidayMarineDay'
  }
  // 山之日固定在 8 月 11 日。
  if (month === 8 && day === 11) {
    return 'holidayMountainDay'
  }
  // 敬老之日为 9 月第三个星期一。
  if (month === 9 && day === nthWeekdayOfMonth(year, 9, 1, 3)) {
    return 'holidayRespectForTheAgedDay'
  }
  // 秋分日每年略有浮动，也通过近似公式计算。
  if (month === 9 && day === getAutumnalEquinoxDay(year)) {
    return 'holidayAutumnalEquinox'
  }
  // 体育之日固定为 10 月第二个星期一。
  if (month === 10 && day === nthWeekdayOfMonth(year, 10, 1, 2)) {
    return 'holidaySportsDay'
  }
  // 文化之日固定在 11 月 3 日。
  if (month === 11 && day === 3) {
    return 'holidayCultureDay'
  }
  // 勤劳感谢之日固定在 11 月 23 日。
  if (month === 11 && day === 23) {
    return 'holidayLaborThanksgivingDay'
  }

  // 未命中任何基础节假日时返回空字符串，交给上层继续判断周末或普通日。
  return ''
}

// 振替休日需要知道前一天是否为周日节假日，因此单独放到二次推导层处理。
function getObservedHolidayKey(parts) {
  // 先取前一天日期，判断是否因为周日节假日顺延出今天这一天。
  const previousDate = new Date(parts.year, parts.month - 1, parts.day - 1)
  // 前一天的基础节日若不存在，则今天不可能是因其带出的振替休日。
  const previousHolidayKey = getBaseHolidayKey({
    year: previousDate.getFullYear(),
    month: previousDate.getMonth() + 1,
    day: previousDate.getDate()
  })
  // 只有“前一天是节假日且落在周日”时，今天才应显示振替休日。
  if (previousHolidayKey && previousDate.getDay() === 0) {
    return 'holidaySubstitute'
  }
  // 没命中振替休日时返回空，交给后续规则继续判断。
  return ''
}

// 国民之休日用于夹在两个节假日中间的普通工作日，常见于 9 月连休场景。
function getBridgeHolidayKey(parts) {
  // 先拿到前后一天，判断当前日期是否被两侧法定节假日夹住。
  const previousDate = new Date(parts.year, parts.month - 1, parts.day - 1)
  const nextDate = new Date(parts.year, parts.month - 1, parts.day + 1)
  // 日本规则里国民之休日不会落在周日，因此先快速排除周日。
  if (toLocalDate(parts).getDay() === 0) {
    return ''
  }
  // 只要前后两天都属于基础节假日，当前这天就标成国民之休日。
  const previousHoliday = getBaseHolidayKey({
    year: previousDate.getFullYear(),
    month: previousDate.getMonth() + 1,
    day: previousDate.getDate()
  })
  const nextHoliday = getBaseHolidayKey({
    year: nextDate.getFullYear(),
    month: nextDate.getMonth() + 1,
    day: nextDate.getDate()
  })
  if (previousHoliday && nextHoliday) {
    return 'holidayCitizensHoliday'
  }
  // 否则当前日期仍然不是日本法定红日子。
  return ''
}

// 对外统一暴露“这一天的节日 key”，让组件侧只关心显示和着色，不重复写节假日规则。
export function getJapanHolidayKey(dateText) {
  // 先把日期转成可复用的年月日对象，避免后续规则多次重复解析。
  const parts = parseDateParts(dateText)
  // 优先命中基础法定节假日，这是最稳定的第一层判断。
  const baseHolidayKey = getBaseHolidayKey(parts)
  if (baseHolidayKey) {
    return baseHolidayKey
  }
  // 基础节日没命中时，再判断是否为周日顺延的振替休日。
  const observedHolidayKey = getObservedHolidayKey(parts)
  if (observedHolidayKey) {
    return observedHolidayKey
  }
  // 最后再判断是否为夹在两个节日中间的国民之休日。
  return getBridgeHolidayKey(parts)
}

// 周末判断也统一在工具层完成，组件侧只消费最终的布尔状态和 weekday 序号。
export function getJapanCalendarMeta(dateText) {
  // 解析并构造本地日期对象，统一输出星期和节假日元信息。
  const parts = parseDateParts(dateText)
  const date = toLocalDate(parts)
  const weekday = date.getDay()
  const holidayKey = getJapanHolidayKey(dateText)
  // 对外返回排班表头需要的全部日期语义，避免组件层再拼业务规则。
  return {
    dateText,
    weekday,
    isSaturday: weekday === 6,
    isSunday: weekday === 0,
    holidayKey,
    isHoliday: Boolean(holidayKey)
  }
}
