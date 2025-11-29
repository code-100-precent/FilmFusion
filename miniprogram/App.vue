<script>
	export default {
		globalData: {
			statusBarHeight: 0,
			navbarHeight: 0
		},
		onLaunch: function() {
			console.log('App Launch')
			// 获取系统信息（使用新的 API）
			let statusBarHeight = 44
			try {
				const windowInfo = uni.getWindowInfo()
				statusBarHeight = windowInfo.statusBarHeight || 44
			} catch (e) {
				// 兼容旧版本
				const systemInfo = uni.getSystemInfoSync()
				statusBarHeight = systemInfo.statusBarHeight || 44
			}
			// 计算导航栏总高度
			const navbarHeight = statusBarHeight + 44
			// 全局存储导航栏高度，供页面使用
			const app = getApp()
			if (app) {
				app.globalData = {
					...app.globalData,
					statusBarHeight: statusBarHeight,
					navbarHeight: navbarHeight
				}
			}
			// 设置CSS变量
			if (typeof document !== 'undefined') {
				document.documentElement.style.setProperty('--status-bar-height', statusBarHeight + 'px')
			}
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style lang="scss">
	/* 全局样式重置 */
	page {
		background-color: #f8fafc;
		font-family: -apple-system, BlinkMacSystemFont, "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", SimSun, sans-serif;
		line-height: 1.6;
		box-sizing: border-box;
		
		/* 隐藏页面滚动条 */
		::-webkit-scrollbar {
			width: 0;
			height: 0;
		}
		::-webkit-scrollbar-track {
			display: none;
		}
		::-webkit-scrollbar-thumb {
			display: none;
		}
		/* 兼容火狐浏览器 */
		scrollbar-width: none;
		/* 兼容IE浏览器 */
		-ms-overflow-style: none;
	}
	
	/* 全局隐藏所有元素的滚动条 */
	* {
		/* 隐藏WebKit滚动条 */
		::-webkit-scrollbar {
			display: none !important;
			width: 0 !important;
			height: 0 !important;
		}
		/* 兼容火狐浏览器 */
		scrollbar-width: none !important;
		/* 兼容IE浏览器 */
		-ms-overflow-style: none !important;
	}
	
	/* 针对滚动容器的特殊处理 */
	scroll-view,
	.content,
	.policy-list {
		/* 隐藏滚动条 */
		::-webkit-scrollbar {
			display: none !important;
			width: 0 !important;
			height: 0 !important;
		}
		::-webkit-scrollbar-track {
			display: none !important;
		}
		::-webkit-scrollbar-thumb {
			display: none !important;
		}
		/* 兼容火狐浏览器 */
		scrollbar-width: none !important;
		/* 兼容IE浏览器 */
		-ms-overflow-style: none !important;
	}
	
	/* 全局box-sizing */
	* {
		box-sizing: border-box;
	}

	/* 为页面内容预留顶部导航栏空间 - 使用固定高度 */
	.page-container {
		margin-top: 176rpx; /* 状态栏44rpx + 导航栏88rpx + 安全距离44rpx */
		min-height: calc(100vh - 176rpx);
		box-sizing: border-box;
	}

	/* 主内容容器 */
	.main-container {
		padding: 30rpx;
		min-height: calc(100vh - 176rpx);
	}

	/* 安全区域容器 - 推荐使用这个 */
	.safe-container {
		padding-top: 176rpx; /* 为导航栏预留空间 */
		min-height: 100vh;
		box-sizing: border-box;
	}

	/* 内容区域 */
	.content-area {
		padding: 30rpx;
	}

	/* 通用卡片样式 */
	.card {
		background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
		border-radius: 24rpx;
		padding: 40rpx;
		margin-bottom: 30rpx;
		box-shadow: 
			0 8rpx 32rpx rgba(0, 0, 0, 0.04),
			0 2rpx 8rpx rgba(0, 0, 0, 0.02);
		border: 1rpx solid rgba(255, 255, 255, 0.8);
		backdrop-filter: blur(20rpx);
		transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	}

	.card:active {
		transform: translateY(-4rpx);
		box-shadow: 
			0 16rpx 48rpx rgba(0, 0, 0, 0.08),
			0 4rpx 16rpx rgba(0, 0, 0, 0.04);
	}

	/* 通用按钮样式 */
	.btn-primary {
		background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
		color: #ffffff;
		border-radius: 16rpx;
		padding: 24rpx 48rpx;
		font-size: 32rpx;
		font-weight: 600;
		border: none;
		box-shadow: 0 8rpx 24rpx rgba(79, 70, 229, 0.3);
		transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
		position: relative;
		overflow: hidden;
	}

	.btn-primary::before {
		content: '';
		position: absolute;
		top: 0;
		left: -100%;
		width: 100%;
		height: 100%;
		background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
		transition: left 0.5s;
	}

	.btn-primary:active::before {
		left: 100%;
	}

	.btn-primary:active {
		transform: scale(0.98);
		box-shadow: 0 4rpx 16rpx rgba(79, 70, 229, 0.4);
	}

	.btn-secondary {
		background: rgba(248, 250, 252, 0.8);
		color: #374151;
		border-radius: 16rpx;
		padding: 24rpx 48rpx;
		font-size: 32rpx;
		font-weight: 500;
		border: 2rpx solid rgba(209, 213, 219, 0.3);
		backdrop-filter: blur(20rpx);
		transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	}

	.btn-secondary:active {
		background: rgba(229, 231, 235, 0.8);
		transform: scale(0.98);
	}

	/* 通用输入框样式 */
	.input-field {
		background: rgba(255, 255, 255, 0.9);
		border: 2rpx solid rgba(209, 213, 219, 0.3);
		border-radius: 16rpx;
		padding: 24rpx 32rpx;
		font-size: 32rpx;
		color: #374151;
		backdrop-filter: blur(20rpx);
		transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	}

	.input-field:focus {
		border-color: #4f46e5;
		box-shadow: 0 0 0 6rpx rgba(79, 70, 229, 0.1);
		outline: none;
	}

	/* 通用文本样式 */
	.text-primary {
		color: #1f2937;
		font-weight: 600;
	}

	.text-secondary {
		color: #6b7280;
		font-weight: 400;
	}

	.text-muted {
		color: #9ca3af;
		font-size: 28rpx;
	}

	/* 通用间距 */
	.mb-small { margin-bottom: 20rpx; }
	.mb-medium { margin-bottom: 40rpx; }
	.mb-large { margin-bottom: 60rpx; }

	.mt-small { margin-top: 20rpx; }
	.mt-medium { margin-top: 40rpx; }
	.mt-large { margin-top: 60rpx; }

	/* 通用布局 */
	.flex-row {
		display: flex;
		flex-direction: row;
		align-items: center;
	}

	.flex-column {
		display: flex;
		flex-direction: column;
	}

	.flex-center {
		justify-content: center;
		align-items: center;
	}

	.flex-between {
		justify-content: space-between;
	}

	.flex-1 {
		flex: 1;
	}
</style>
