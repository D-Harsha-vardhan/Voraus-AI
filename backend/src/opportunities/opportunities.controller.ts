import { Controller, Get, Post, Param, Req, Body } from '@nestjs/common';
import { OpportunitiesService } from './opportunities.service';

@Controller('opportunities')
export class OpportunitiesController {
  constructor(private readonly opportunitiesService: OpportunitiesService) {}

  @Get()
  async getOpportunities() {
    return this.opportunitiesService.getAllOpportunities();
  }

  @Get(':id')
  async getOpportunity(@Param('id') id: string) {
    return this.opportunitiesService.getOpportunity(id);
  }

  @Post('search')
  async searchOpportunities(@Req() req: any) {
    const applicantId = req.headers['x-applicant-id'];
    return this.opportunitiesService.matchOpportunitiesForApplicant(applicantId);
  }
}
