# Tour API 使用示例

## 📝 创建线路（包含多个 Day）

### 请求示例：创建一个包含 3 天行程的线路

**POST** `/api/tour/admin/create`

```json
{
  "name": "红色峥嵘·长征影视主题红色游线路",
  "description": "依托雅安红军长征、西康解放红色遗址，结合《长征》《陈云出川》《百丈关战役》等红色影视剧取景地打造",
  "days": [
    {
      "name": "西康解放·百丈关战役影视线",
      "day": "Day1",
      "attractions": [
        {
          "name": "红军百丈关战役纪念馆+烈士纪念园",
          "highlights": "参观红军武器、作战手稿实物，瞻仰9356座无名英烈纪念标识，沉浸式观看战役复原影像",
          "locationId": "1,2",
          "dramaId": "10,11",
          "hotelId": "5"
        },
        {
          "name": "八一路秘密电台遗址",
          "highlights": "参观电波史料、复刻地下党员工作环境，开展入党誓词重温活动",
          "locationId": "3",
          "dramaId": "12",
          "hotelId": "5,6"
        }
      ]
    },
    {
      "name": "雨城红色记忆线",
      "day": "Day2",
      "attractions": [
        {
          "name": "雅安市博物馆",
          "highlights": "红军长征在雅安专题展，观看历史纪录片",
          "locationId": "4,5",
          "dramaId": "13",
          "hotelId": "7"
        },
        {
          "name": "上里古镇",
          "highlights": "红军石刻标语，体验红军食堂",
          "locationId": "6",
          "dramaId": "14",
          "hotelId": "8,9"
        }
      ]
    },
    {
      "name": "长征国家文化公园体验线",
      "day": "Day3",
      "attractions": [
        {
          "name": "夹金山红军纪念馆",
          "highlights": "攀登体验，重走长征路",
          "locationId": "7,8,9",
          "dramaId": "15,16",
          "hotelId": "10"
        }
      ]
    }
  ]
}
```

---

## ✅ 返回数据结构

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "红色峥嵘·长征影视主题红色游线路",
    "description": "依托雅安红军长征、西康解放红色遗址...",
    "days": [
      {
        "id": 1,
        "tourId": 1,
        "name": "西康解放·百丈关战役影视线",
        "day": "Day1",
        "attractions": [
          {
            "id": 1,
            "dayId": 1,
            "tourId": 1,
            "name": "红军百丈关战役纪念馆+烈士纪念园",
            "highlights": "参观红军武器、作战手稿实物...",
            "locationId": "1,2",
            "dramaId": "10,11",
            "hotelId": "5",
            "createdAt": "2025-01-15 10:00:00",
            "updatedAt": "2025-01-15 10:00:00"
          },
          {
            "id": 2,
            "dayId": 1,
            "tourId": 1,
            "name": "八一路秘密电台遗址",
            "highlights": "参观电波史料...",
            "locationId": "3",
            "dramaId": "12",
            "hotelId": "5,6",
            "createdAt": "2025-01-15 10:00:00",
            "updatedAt": "2025-01-15 10:00:00"
          }
        ],
        "createdAt": "2025-01-15 10:00:00",
        "updatedAt": "2025-01-15 10:00:00"
      },
      {
        "id": 2,
        "tourId": 1,
        "name": "雨城红色记忆线",
        "day": "Day2",
        "attractions": [
          {
            "id": 3,
            "dayId": 2,
            "tourId": 1,
            "name": "雅安市博物馆",
            "highlights": "红军长征在雅安专题展...",
            "locationId": "4,5",
            "dramaId": "13",
            "hotelId": "7",
            "createdAt": "2025-01-15 10:00:00",
            "updatedAt": "2025-01-15 10:00:00"
          },
          {
            "id": 4,
            "dayId": 2,
            "tourId": 1,
            "name": "上里古镇",
            "highlights": "红军石刻标语...",
            "locationId": "6",
            "dramaId": "14",
            "hotelId": "8,9",
            "createdAt": "2025-01-15 10:00:00",
            "updatedAt": "2025-01-15 10:00:00"
          }
        ],
        "createdAt": "2025-01-15 10:00:00",
        "updatedAt": "2025-01-15 10:00:00"
      },
      {
        "id": 3,
        "tourId": 1,
        "name": "长征国家文化公园体验线",
        "day": "Day3",
        "attractions": [
          {
            "id": 5,
            "dayId": 3,
            "tourId": 1,
            "name": "夹金山红军纪念馆",
            "highlights": "攀登体验，重走长征路",
            "locationId": "7,8,9",
            "dramaId": "15,16",
            "hotelId": "10",
            "createdAt": "2025-01-15 10:00:00",
            "updatedAt": "2025-01-15 10:00:00"
          }
        ],
        "createdAt": "2025-01-15 10:00:00",
        "updatedAt": "2025-01-15 10:00:00"
      }
    ],
    "createdAt": "2025-01-15 10:00:00",
    "updatedAt": "2025-01-15 10:00:00"
  }
}
```

---

## 🔄 更新线路（修改 Day 和 Attraction）

**PUT** `/api/tour/admin/update/1`

```json
{
  "name": "红色峥嵘·长征影视主题红色游线路（更新版）",
  "description": "更新后的描述...",
  "days": [
    {
      "name": "西康解放·百丈关战役影视线（更新）",
      "day": "Day1",
      "attractions": [
        {
          "name": "红军百丈关战役纪念馆（新版）",
          "highlights": "更新后的亮点...",
          "locationId": "1,2,3",
          "dramaId": "10",
          "hotelId": "5"
        }
      ]
    },
    {
      "name": "新增的第二天行程",
      "day": "Day2",
      "attractions": [
        {
          "name": "新景点",
          "highlights": "新亮点",
          "locationId": "4",
          "dramaId": "11",
          "hotelId": "6"
        }
      ]
    }
  ]
}
```

> **注意**：更新时会先删除旧的所有 Days 和 Attractions，然后重新创建新的数据

---

## 🔍 查询线路（自动返回所有 Day）

**GET** `/api/tour/1`

自动返回完整的嵌套结构，包含所有 Day1, Day2, Day3... 及其下的所有 Attractions

---

## 🗑️ 删除线路（级联删除）

**DELETE** `/api/tour/admin/delete/1`

会自动级联逻辑删除：
- Tour 主记录
- 所有关联的 Days（Day1, Day2, Day3...）
- 所有关联的 Attractions

---

## 📊 数据流程

```
创建 Tour
  ├── Tour 主表插入
  ├── Day1 插入
  │   ├── Attraction 1 插入
  │   └── Attraction 2 插入
  ├── Day2 插入
  │   ├── Attraction 3 插入
  │   └── Attraction 4 插入
  └── Day3 插入
      └── Attraction 5 插入
```

---

## ✅ 核心特性

1. **支持任意多个 Day**：可以有 Day1, Day2, Day3... Day10 等
2. **每个 Day 可以有多个 Attraction**：灵活配置
3. **事务保证**：创建/更新/删除全部在事务中，保证数据一致性
4. **级联操作**：删除 Tour 自动删除所有关联数据
5. **自动排序**：Days 按 `day` 字段排序，Attractions 按 `id` 排序

---

## 🎯 适用场景

✅ 1天行程 → 1个 Day + N个 Attractions
✅ 3天行程 → 3个 Days，每天 N个 Attractions
✅ 7天行程 → 7个 Days，每天 N个 Attractions
✅ 任意天数行程 → 完全支持

---

**代码已完全支持你提到的 Day1, Day2, Day3 的场景！** 🎉
