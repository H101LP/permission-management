/**
 * 获取当前用户的认证令牌
 */
export function getToken() {
  return localStorage.getItem('tokenKey')
}

/**
 * 设置(保存)用户当前令牌
 */
export function setToken(token) {
  localStorage.setItem('tokenKey', token)
}

/**
 * 删除当前用户的认证令牌
 */
export function removeToken() {
  localStorage.removeItem('tokenKey')
}

