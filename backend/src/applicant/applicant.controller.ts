import { Controller, Get, Put, Body, Req, UseGuards } from '@nestjs/common';
import { ApplicantService } from './applicant.service';

// In a real app we'd use a JwtAuthGuard
// @UseGuards(JwtAuthGuard)
@Controller('applicant')
export class ApplicantController {
  constructor(private readonly applicantService: ApplicantService) {}

  @Get('me')
  async getProfile(@Req() req: any) {
    // For demo purposes, assuming applicantId is passed in headers or body
    const applicantId = req.headers['x-applicant-id'];
    return this.applicantService.getProfile(applicantId);
  }

  @Put('profile')
  async updateProfile(@Req() req: any, @Body() updateData: any) {
    const applicantId = req.headers['x-applicant-id'];
    return this.applicantService.updateProfile(applicantId, updateData);
  }
}
