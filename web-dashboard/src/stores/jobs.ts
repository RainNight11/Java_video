import { defineStore } from "pinia";
import axios from "axios";

export interface VoiceClonePayload {
  file: File;
  label?: string;
}

export interface StoryPayload {
  script: string;
  mode: "CREATIVE" | "BROADCAST";
  voiceId: string;
  title?: string;
  avatarImageUrl?: string;
}

export interface JobStatus {
  jobId: string;
  status: string;
  progress: number;
  message: string;
  downloadUrl?: string;
}

interface State {
  jobs: Record<string, JobStatus>;
  loading: boolean;
  error: string | null;
}

export const useJobsStore = defineStore("jobs", {
  state: (): State => ({
    jobs: {},
    loading: false,
    error: null
  }),
  actions: {
    async cloneVoice(payload: VoiceClonePayload): Promise<{ voiceId: string }> {
      this.loading = true;
      this.error = null;
      try {
        const formData = new FormData();
        formData.append("file", payload.file);
        if (payload.label) {
          formData.append("label", payload.label);
        }
        const response = await axios.post("/api/v1/voices", formData, {
          headers: { "Content-Type": "multipart/form-data" }
        });
        return { voiceId: response.data.voiceId };
      } catch (error) {
        this.error = "语音克隆失败，请稍后重试。";
        throw error;
      } finally {
        this.loading = false;
      }
    },
    async submitStory(payload: StoryPayload): Promise<string> {
      this.loading = true;
      this.error = null;
      try {
        const response = await axios.post("/api/v1/stories", payload);
        const jobId: string = response.data.jobId;
        this.jobs[jobId] = {
          jobId,
          status: "RECEIVED",
          progress: 0,
          message: "Job accepted"
        };
        this.trackJob(jobId);
        return jobId;
      } catch (error) {
        this.error = "生成任务提交失败。";
        throw error;
      } finally {
        this.loading = false;
      }
    },
    async trackJob(jobId: string) {
      try {
        const response = await axios.get(`/api/v1/jobs/${jobId}`);
        this.jobs[jobId] = response.data;
        if (response.data.status !== "COMPLETED" && response.data.status !== "FAILED") {
          setTimeout(() => this.trackJob(jobId), 1500);
        }
      } catch {
        this.error = "任务状态查询失败。";
      }
    },
    async fetchAllJobs() {
        this.loading = true;
        this.error = null;
        try {
            const response = await axios.get("/api/v1/jobs");
            // Assuming the response is an array of JobStatus objects
            const jobsArray = response.data as JobStatus[];
            const jobsMap: Record<string, JobStatus> = {};
            for (const job of jobsArray) {
                jobsMap[job.jobId] = job;
            }
            this.jobs = jobsMap;
        } catch (error) {
            this.error = "获取任务列表失败。";
        } finally {
            this.loading = false;
        }
    }
  }
});
