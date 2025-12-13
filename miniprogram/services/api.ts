/**
 * API 服务层 - 对接后端接口
 */
import { http } from '../utils/http'

// 基础响应类型
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

// 分页响应类型（后端实际返回格式）
export interface PageResponse<T> {
  code: number
  message: string
  data: T[]  // 直接是数组
  pagination: {
    currentPage: number
    pageSize: number
    totalItems: number
    totalPages: number
  }
}

// ==================== 用户相关 ====================

export interface LoginDTO {
  username: string
  password: string
}

export interface RegisterDTO {
  username: string
  password: string
  phoneNumber: string
}

export interface UserInfo {
  id: number
  username: string
  phoneNumber: string
  avatar: string
  role: string
  enabled: boolean
}

export interface LoginResponse {
  token: string
  userInfo: UserInfo
}

// 用户登录
export const userLogin = (params: LoginDTO): Promise<ApiResponse<LoginResponse>> => {
  return http<LoginResponse>({
    url: '/user/login',
    method: 'POST',
    data: params
  }) as Promise<ApiResponse<LoginResponse>>
}

// 用户注册
export const userRegister = (params: RegisterDTO): Promise<ApiResponse<LoginResponse>> => {
  return http<LoginResponse>({
    url: '/user/register',
    method: 'POST',
    data: params
  }) as Promise<ApiResponse<LoginResponse>>
}

// 获取当前用户信息
export const getCurrentUserInfo = (): Promise<ApiResponse<UserInfo>> => {
  return http<UserInfo>({
    url: '/user/info',
    method: 'GET'
  }) as Promise<ApiResponse<UserInfo>>
}

// 更新用户信息
export const updateUserInfo = (data: { username?: string; phoneNumber?: string }): Promise<ApiResponse<UserInfo>> => {
  return http<UserInfo>({
    url: '/user/info',
    method: 'PUT',
    data
  }) as Promise<ApiResponse<UserInfo>>
}

// 修改密码
export const changePassword = (data: { oldPassword: string; newPassword: string }): Promise<ApiResponse<void>> => {
  return http<void>({
    url: '/user/password',
    method: 'PUT',
    data
  }) as Promise<ApiResponse<void>>
}

// 退出登录
export const userLogout = (): Promise<ApiResponse<void>> => {
  return http<void>({
    url: '/user/logout',
    method: 'POST'
  }) as Promise<ApiResponse<void>>
}

// ==================== 文章相关 ====================

export interface Article {
  id: number
  title: string
  issueUnit: string
  issueTime: string
  content: string
  createdAt?: string
  updatedAt?: string
}

// 分页获取文章列表
export const getArticlePage = (params: { current?: number; size?: number; keyword?: string }): Promise<PageResponse<Article>> => {
  return http<Article[]>({
    url: '/article/page',
    method: 'GET',
    data: params
  }) as Promise<PageResponse<Article>>
}

// 获取文章详情
export const getArticleById = (id: number): Promise<ApiResponse<Article>> => {
  return http<Article>({
    url: `/article/${id}`,
    method: 'GET'
  }) as Promise<ApiResponse<Article>>
}

// ==================== 拍摄场地相关 ====================

export interface Location {
  id: number
  name: string
  type: string
  status: number
  locationDescription: string
  contactPhone: string
  contactName: string
  address: string
  price: number
  createdAt?: string
  updatedAt?: string
}

// 分页获取拍摄场地列表
export const getLocationPage = (params: { current?: number; size?: number; keyword?: string }): Promise<PageResponse<Location>> => {
  return http<Location[]>({
    url: '/location/page',
    method: 'GET',
    data: params
  }) as Promise<PageResponse<Location>>
}

// 获取拍摄场地详情
export const getLocationById = (id: number): Promise<ApiResponse<Location>> => {
  return http<Location>({
    url: `/location/${id}`,
    method: 'GET'
  }) as Promise<ApiResponse<Location>>
}

// ==================== 协拍服务相关 ====================

export interface Shoot {
  id: number
  name: string
  description: string
  price: number
  status: number
  address: string
  phone: string
  contactName: string
  createdAt?: string
  updatedAt?: string
}

// 分页获取协拍服务列表
export const getShootPage = (params: { current?: number; size?: number; keyword?: string }): Promise<PageResponse<Shoot>> => {
  return http<Shoot[]>({
    url: '/shoot/page',
    method: 'GET',
    data: params
  }) as Promise<PageResponse<Shoot>>
}

// 获取协拍服务详情
export const getShootById = (id: number): Promise<ApiResponse<Shoot>> => {
  return http<Shoot>({
    url: `/shoot/${id}`,
    method: 'GET'
  }) as Promise<ApiResponse<Shoot>>
}

// ==================== 影视剧备案相关 ====================

export interface Report {
  id: number
  name: string
  type: string
  genre: string
  episodes: number
  investAmount: number
  mainCreators: string
  leadProducer: string
  producerUnit: string
  startDate: string
  endDate: string
  crewScale: string
  contact: string
  phoneNumber: string
  crewPosition: string
  createdAt?: string
  updatedAt?: string
}

export interface CreateReportDTO {
  name: string
  type: string
  genre: string
  episodes: number
  investAmount: number
  mainCreators: string
  leadProducer: string
  producerUnit: string
  startDate: string
  endDate: string
  crewScale: string
  contact: string
  phoneNumber: string
  crewPosition: string
}

// 分页获取影视剧备案列表
export const getReportPage = (params: { current?: number; size?: number; keyword?: string }): Promise<PageResponse<Report>> => {
  return http<Report[]>({
    url: '/report/page',
    method: 'GET',
    data: params
  }) as Promise<PageResponse<Report>>
}

// 获取影视剧备案详情
export const getReportById = (id: number): Promise<ApiResponse<Report>> => {
  return http<Report>({
    url: `/report/${id}`,
    method: 'GET'
  }) as Promise<ApiResponse<Report>>
}

// 创建影视剧备案
export const createReport = (data: CreateReportDTO): Promise<ApiResponse<Report>> => {
  return http<Report>({
    url: '/report',
    method: 'POST',
    data
  }) as Promise<ApiResponse<Report>>
}

// 获取我的报备列表
export const getMyReportPage = (params: { current?: number; size?: number }): Promise<PageResponse<Report>> => {
  return http<Report[]>({
    url: '/report/my',
    method: 'GET',
    data: params
  }) as Promise<PageResponse<Report>>
}

// ==================== 反馈相关 ====================

export interface Feedback {
  id: number
  userId: number
  content: string
  status: string
  type: string
  createdAt?: string
  updatedAt?: string
}

export interface CreateFeedbackDTO {
  content: string
  type: string
}

export interface FeedbackDTO {
  content?: string
  type?: string
  status?: string
}

// 创建反馈
export const createFeedback = (data: CreateFeedbackDTO): Promise<ApiResponse<Feedback>> => {
  return http<Feedback>({
    url: '/feedback',
    method: 'POST',
    data
  }) as Promise<ApiResponse<Feedback>>
}

// 获取我的反馈列表
export const getMyFeedbackPage = (params: { current?: number; size?: number }): Promise<PageResponse<Feedback>> => {
  return http<Feedback[]>({
    url: '/feedback/my',
    method: 'GET',
    data: params
  }) as Promise<PageResponse<Feedback>>
}

// 获取反馈详情
export const getFeedbackDetail = (id: number): Promise<ApiResponse<Feedback>> => {
  return http<Feedback>({
    url: `/feedback/${id}`,
    method: 'GET'
  }) as Promise<ApiResponse<Feedback>>
}

// 更新反馈
export const updateFeedback = (id: number, data: FeedbackDTO): Promise<ApiResponse<Feedback>> => {
  return http<Feedback>({
    url: `/feedback/${id}`,
    method: 'PUT',
    data
  }) as Promise<ApiResponse<Feedback>>
}

// 删除反馈
export const deleteFeedback = (id: number): Promise<ApiResponse<void>> => {
  return http<void>({
    url: `/feedback/${id}`,
    method: 'DELETE'
  }) as Promise<ApiResponse<void>>
}
