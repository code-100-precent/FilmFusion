export const createReport = async (params) => {
    await delay()
    console.log('剧组报备信息:', params)

    // 模拟保存到数据库
    const reportId = 'R' + Date.now()

    return {
        code: 200,
        message: '报备成功',
        data: {
            reportId,
            status: '待审核',
            submitTime: new Date().toISOString()
        }
    }
}

// 文件上传(模拟)
export const uploadFile = async (file) => {
    await delay(800)

    // 模拟文件上传,返回文件URL
    return {
        code: 200,
        message: '上传成功',
        data: {
            url: 'https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/' + Date.now() + '.pdf',
            fileName: file.name || 'document.pdf',
            fileSize: file.size || 1024000
        }
    }
}

// 根据ID获取场地详情
export const getLocationById = async (id) => {
    await delay()

    const mockLocations = [
        {
            id: 1,
            name: '碧峰峡景区',
            type: '自然景观',
            locationDescription: '国家5A级旅游景区,以雅安大熊猫基地和峡谷风光闻名。景区内山峦叠翠,峡谷幽深,瀑布飞泻,是拍摄自然风光和动物题材的绝佳场所。',
            address: '雅安市雨城区碧峰峡镇',
            cover: 'https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/bifengxia.jpg',
            price: 5000,
            contactName: '张经理',
            contactPhone: '13800138001',
            status: 1,
            bestShootingTime: '春季(3-5月)、秋季(9-11月)',
            facilities: '停车场、餐饮、住宿、观光车',
            latitude: 30.0123,
            longitude: 102.9876
        },
        {
            id: 2,
            name: '上里古镇',
            type: '人文景观',
            locationDescription: '四川十大古镇之一,保存完好的明清建筑群。古镇依山傍水,石板街道纵横交错,古朴典雅,是拍摄古装剧和历史题材的理想场所。',
            address: '雅安市雨城区上里镇',
            cover: 'https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/shangli.jpg',
            price: 3000,
            contactName: '李经理',
            contactPhone: '13800138002',
            status: 1,
            bestShootingTime: '全年适宜',
            facilities: '古建筑、石板路、特色民宿、茶馆',
            latitude: 30.1234,
            longitude: 102.8765
        },
        {
            id: 3,
            name: '蒙顶山茶园',
            type: '自然景观',
            locationDescription: '世界茶文化发源地,拥有万亩生态茶园。茶园层层叠叠,绿意盎然,云雾缭绕,是拍摄茶文化和田园风光的最佳选择。',
            address: '雅安市名山区蒙顶山镇',
            cover: 'https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/mengdingshan.jpg',
            price: 4000,
            contactName: '王经理',
            contactPhone: '13800138003',
            status: 1,
            bestShootingTime: '春季采茶季(3-4月)',
            facilities: '茶园、观景台、茶文化体验馆、停车场',
            latitude: 30.0678,
            longitude: 103.0345
        },
        {
            id: 4,
            name: '雅安市文化中心',
            type: '城市场景',
            locationDescription: '现代化文化设施,适合城市题材拍摄。建筑设计现代简约,内部设施完善,可满足各类室内拍摄需求。',
            address: '雅安市雨城区雨城大道',
            cover: null,
            price: 2000,
            contactName: '赵经理',
            contactPhone: '13800138004',
            status: 1,
            bestShootingTime: '全年适宜',
            facilities: '演播厅、展览馆、会议室、化妆间',
            latitude: 30.0456,
            longitude: 102.9987
        }
    ]

    const location = mockLocations.find(l => l.id === id)

    if (!location) {
        return {
            code: 404,
            message: '场地不存在',
            data: null
        }
    }

    return {
        code: 200,
        message: '查询成功',
        data: location
    }
}
