// 使用 Mock 数据，不调用真实后端接口
import mockApi from '../utils/mockData'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface VisitorLoginParams {
  phone: string
  password: string
}

export interface VisitorRegisterParams {
  phone: string
  password: string
  name: string
  identity: string
}

export interface VisitorVO {
  visitorId: string
  name: string
  phone: string
  identity: string
  registerTime?: string
  lastLoginTime?: string
  token?: string
  avatar?: string
}

export interface POI {
  poiId: string
  name: string
  x?: number
  y?: number
  type: string
  intro: string
  imageUrl?: string
  openTime?: string
  tags?: string[]
}

export interface Story {
  storyId: number
  title: string
  content: string
  publishTime?: string
  alumniId?: number
  poiId?: string
  imageUrl?: string
  emotionTags?: Array<{ tagId: number; tagName: string }>
}

export interface Route {
  routeId: number
  routeName: string
  routeDesc?: string
  duration?: number
  suitableIdentity?: string
  poisCount?: number
}

export interface Collection {
  id?: number
  poiId: string
  visitorId: string
  name?: string
  collectTime?: string
}

export interface BrowseRecord {
  id?: number
  poiId: string
  visitorId: string
  name?: string
  browseTime?: string
}

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
  updatedAt?: string
}

export interface ShootService {
  id: number
  name: string
  description: string
  price: number
  status: number
  address: string
  phone: string
  contactName: string
  updatedAt?: string
}

export interface Article {
  id: number
  title: string
  issueUnit: string
  issueTime: string
  content: string
  coverUrl?: string
}

export interface ReportPayload {
  name: string
  contact: string
  phoneNumber: string
  startDate: string
  endDate: string
  crewScale: string
  leadProducer: string
  producerUnit: string
  location: string
  remark?: string
  fileIds?: string[]
}

export interface FeedbackPayload {
  type: string
  content: string
}

// 所有API调用已改为使用Mock数据，不再需要HTTP请求和认证头

export const visitorLogin = async (params: VisitorLoginParams): Promise<ApiResponse<VisitorVO>> => {
  return mockApi.visitorLogin(params) as Promise<ApiResponse<VisitorVO>>
}

export const visitorRegister = async (params: VisitorRegisterParams): Promise<ApiResponse<VisitorVO>> => {
  return mockApi.visitorRegister(params) as Promise<ApiResponse<VisitorVO>>
}

export const getVisitorInfo = async (): Promise<ApiResponse<VisitorVO>> => {
  return mockApi.getVisitorInfo() as Promise<ApiResponse<VisitorVO>>
}

export const updateVisitorInfo = async (params: Partial<VisitorVO>): Promise<ApiResponse<VisitorVO>> => {
  return mockApi.updateVisitorInfo(params) as Promise<ApiResponse<VisitorVO>>
}

export const visitorLogout = async (): Promise<ApiResponse<string>> => {
  return mockApi.visitorLogout() as Promise<ApiResponse<string>>
}

export const getPoiList = async (params?: { type?: string; keyword?: string }): Promise<ApiResponse<POI[]>> => {
  return mockApi.getPoiList(params) as Promise<ApiResponse<POI[]>>
}

export const getPoiDetail = async (poiId: string): Promise<ApiResponse<POI & { stories?: Story[] }>> => {
  return mockApi.getPoiDetail(poiId) as Promise<ApiResponse<POI & { stories?: Story[] }>>
}

export const getStoryList = async (params?: { poiId?: string; keyword?: string }): Promise<ApiResponse<Story[]>> => {
  return mockApi.getStoryList(params) as Promise<ApiResponse<Story[]>>
}

export const getStoryDetail = async (storyId: number): Promise<ApiResponse<Story>> => {
  return mockApi.getStoryDetail(storyId) as Promise<ApiResponse<Story>>
}

export const getRouteList = async (params?: { identity?: string; duration?: number }): Promise<ApiResponse<Route[]>> => {
  return mockApi.getRouteList(params) as Promise<ApiResponse<Route[]>>
}

export const getRouteDetail = async (routeId: number): Promise<ApiResponse<Route>> => {
  return mockApi.getRouteDetail(routeId) as Promise<ApiResponse<Route>>
}

export const generateRoute = async (params: { identity: string; duration: number }): Promise<ApiResponse<Route>> => {
  return mockApi.generateRoute(params) as Promise<ApiResponse<Route>>
}

export const getLocations = async (): Promise<ApiResponse<Location[]>> => {
  return mockApi.getLocations() as Promise<ApiResponse<Location[]>>
}

export const getShootServices = async (): Promise<ApiResponse<ShootService[]>> => {
  return mockApi.getShootServices() as Promise<ApiResponse<ShootService[]>>
}

export const getArticles = async (): Promise<ApiResponse<Article[]>> => {
  return mockApi.getArticles() as Promise<ApiResponse<Article[]>>
}

export const submitReport = async (payload: ReportPayload): Promise<ApiResponse<boolean>> => {
  return mockApi.submitReport(payload) as Promise<ApiResponse<boolean>>
}

export const uploadReportFile = async (filePath: string): Promise<ApiResponse<{ fileId: string; url: string }>> => {
  return mockApi.uploadReportFile(filePath) as Promise<ApiResponse<{ fileId: string; url: string }>>
}

export const addCollect = async (params: { poiId: string; visitorId: string }): Promise<ApiResponse<boolean>> => {
  return mockApi.addCollect(params) as Promise<ApiResponse<boolean>>
}

export const getCollectList = async (visitorId: string): Promise<ApiResponse<Collection[]>> => {
  return mockApi.getCollectList(visitorId) as Promise<ApiResponse<Collection[]>>
}

export const addBrowse = async (params: { poiId: string; visitorId: string }): Promise<ApiResponse<boolean>> => {
  return mockApi.addBrowse(params) as Promise<ApiResponse<boolean>>
}

export const getBrowseList = async (visitorId: string): Promise<ApiResponse<BrowseRecord[]>> => {
  return mockApi.getBrowseList(visitorId) as Promise<ApiResponse<BrowseRecord[]>>
}

export const getTagList = async (): Promise<ApiResponse<Array<{ tagId: number; tagName: string }>>> => {
  return mockApi.getTagList() as Promise<ApiResponse<Array<{ tagId: number; tagName: string }>>>
}

export const changePassword = async (oldPassword: string, newPassword: string): Promise<ApiResponse<string>> => {
  return mockApi.changePassword(oldPassword, newPassword) as Promise<ApiResponse<string>>
}

export const submitFeedback = async (params: FeedbackPayload): Promise<ApiResponse<boolean>> => {
  return mockApi.submitFeedback(params) as Promise<ApiResponse<boolean>>
}

export const getAlumniList = async (params?: { keyword?: string }): Promise<
  ApiResponse<Array<{ alumniId: number; name: string; gradYear?: string; major?: string }>>
> => {
  return mockApi.getAlumniList(params) as Promise<ApiResponse<Array<{ alumniId: number; name: string; gradYear?: string; major?: string }>>>
}








