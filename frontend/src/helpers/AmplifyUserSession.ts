import { Auth } from 'aws-amplify'
import type { AmplifyCustomProperties } from '@/dto/CommonTypesDto'

const amplifyUserSession: AmplifyCustomProperties = {
  user: undefined,
  async logIn(provider: string) {
    try {
      await Auth.federatedSignIn({
        customProvider: `DEV-${provider.toUpperCase()}`
      })
      await this.loadDetails()
    } catch (e) {
      this.user = undefined
    }
  },
  async logOut() {
    try {
      await Auth.signOut()
      this.user = undefined
    } catch (e) {}
  },
  async loadDetails() {
    if (this.user === undefined) {
      try {
        const response = await Auth.currentSession()
        if (response) {
          this.user = {
            name: response.getIdToken().payload['custom:idp_display_name'],
            provider: response.getIdToken().payload['custom:idp_name'],
            userId: response.getIdToken().payload['custom:idp_user_id'],
            email: response.getIdToken().payload.email
          }
        }
      } catch (e) {
        this.user = undefined
      }
    }
  },
  async isLoggedIn() {
    await this.loadDetails()
    return this.user !== undefined
  }
}

export default amplifyUserSession
